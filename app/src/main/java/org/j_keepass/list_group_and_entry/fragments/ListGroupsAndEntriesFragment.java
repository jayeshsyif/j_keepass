package org.j_keepass.list_group_and_entry.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.j_keepass.R;
import org.j_keepass.databinding.ListGroupsAndEntriesFragmentBinding;
import org.j_keepass.db.event.operations.Db;
import org.j_keepass.events.loading.LoadingEvent;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.list_db.dtos.GroupEntryType;
import org.j_keepass.list_group_and_entry.adapters.ListGroupsAndEntriesAdapter;
import org.j_keepass.list_group_and_entry.enums.GroupsAndEntriesAction;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ListGroupsAndEntriesFragment extends Fragment implements LoadingEvent, ReloadEvent {
    ArrayList<ExecutorService> executorServices = new ArrayList<>();
    private ListGroupsAndEntriesFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.log("List db frag on create");
        super.onCreate(savedInstanceState);
        binding = ListGroupsAndEntriesFragmentBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.log("List group entry frag on create view");
        View view = binding.getRoot();
        register();
        configureClick();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String show = bundle.getString("show");
            if (show != null && show.equals("showEntryOnly")) {
                showAllEntryOnly();
            } else {
                ReloadEventSource.getInstance().reload(ReloadAction.GROUP_UPDATE);
                setGroup(Db.getInstance().getCurrentGroupId());
            }
        }

        return view;
    }

    private void configureClick() {
        binding.searchEntryView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Utils.log("Search query entered is " + query);
                showAllEntryOnly(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        binding.searchEntryView.setOnCloseListener(() -> {
            setGroup(Db.getInstance().getRootGroupId());
            return false;
        });
    }

    private void show(final UUID gId, GroupsAndEntriesAction groupsAndEntriesAction, String query) {
        ExecutorService executor = getExecutor();
        AtomicReference<ListGroupsAndEntriesAdapter> adapter = new AtomicReference<>();
        executor.execute(() -> updateLoadingText(binding.getRoot().getContext().getString(R.string.loading)));
        executor.execute(this::showLoading);
        executor.execute(() -> adapter.set(configureRecyclerView(binding.showGroupEntriesRecyclerView.getContext(), groupsAndEntriesAction)));
        executor.execute(() -> {
            if (Db.getInstance() != null && Db.getInstance().getRootGroupId() != null) {
                listFromGroupId(gId, adapter.get(), groupsAndEntriesAction, query);
            }
        });
        executor.execute(this::dismissLoading);
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void register() {
        LoadingEventSource.getInstance().addListener(this);
        ReloadEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        LoadingEventSource.getInstance().removeListener(this);
        ReloadEventSource.getInstance().removeListener(this);
    }

    @Override
    public void reload(ReloadAction reloadAction) {
        if (reloadAction != null && (reloadAction.name().equals(ReloadAction.GROUP_UPDATE.name()))) {
            show(Db.getInstance().getCurrentGroupId(), GroupsAndEntriesAction.ALL, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("Landing destroy");
        destroy();
    }

    private void destroy() {
        Utils.log("unregister");
        unregister();
        shutDownExecutor();
    }

    private void shutDownExecutor() {
        Utils.log("shutdown all");
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    private ListGroupsAndEntriesAdapter configureRecyclerView(Context context, GroupsAndEntriesAction groupsAndEntriesAction) {
        Utils.log("Configuration recycler view");
        ListGroupsAndEntriesAdapter adapter = new ListGroupsAndEntriesAdapter();
        if (groupsAndEntriesAction != null && groupsAndEntriesAction.name().equals(GroupsAndEntriesAction.ALL_ENTRIES_ONLY.name())) {
            adapter.setShowPath(true);
        }
        try {
            requireActivity().runOnUiThread(() -> {
                Utils.log("Configuration recycler view inside ui thread");
                binding.showGroupEntriesRecyclerView.removeAllViews();
                binding.showGroupEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.showGroupEntriesRecyclerView.setAdapter(adapter);
                Utils.log("Configuration recycler view done");
            });
        } catch (Throwable e) {
            Utils.log("Configuration recycler view, Error " + e.getMessage());
        }
        return adapter;
    }

    private void listFromGroupId(UUID gId, ListGroupsAndEntriesAdapter adapter, GroupsAndEntriesAction groupsAndEntriesAction, String query) {
        final long totalSubs;
        if (groupsAndEntriesAction != null && groupsAndEntriesAction.name().equals(GroupsAndEntriesAction.ALL_ENTRIES_ONLY.name())) {
            if (query == null) {
                totalSubs = Db.getInstance().getAllEntriesCount();
            } else {
                totalSubs = Db.getInstance().getAllEntriesCount(query);
            }
        } else {
            totalSubs = Db.getInstance().getSubGroupsCount(gId) + Db.getInstance().getSubEntriesCount(gId);
        }
        if (totalSubs > 0) {
            try {
                requireActivity().runOnUiThread(() -> {
                    binding.showGroupEntriesRecyclerView.setVisibility(View.VISIBLE);
                    binding.noGroupEntryDeclarationView.setVisibility(View.GONE);
                });
            } catch (Throwable e) {
                //ignore
            }
            updateLoadingText(binding.showGroupEntriesRecyclerView.getContext().getString(R.string.loading) + " [0/" + totalSubs + "]");
            ArrayList<GroupEntryData> subs;
            if (groupsAndEntriesAction != null && groupsAndEntriesAction.name().equals(GroupsAndEntriesAction.ALL_ENTRIES_ONLY.name())) {
                if (query == null) {
                    subs = Db.getInstance().getAllEntries();
                } else {
                    subs = Db.getInstance().getAllEntries(query);
                }
            } else {
                subs = Db.getInstance().getSubGroupsAndEntries(gId);
            }
            int subCountAdded = 0;
            Utils.log("Got groupsAndEntriesAction as " + groupsAndEntriesAction.name());
            for (final GroupEntryData data : subs) {
                Utils.log("Got " + data.name);
                adapter.addValue(data);
                Utils.log("Adding " + data.name);
                subCountAdded++;
                try {
                    requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                } catch (Throwable e) {
                    //ignore
                }
                updateLoadingText(binding.showGroupEntriesRecyclerView.getContext().getString(R.string.loading) + " [" + subCountAdded + "/" + totalSubs + "]");
                Utils.sleepFor1MSec();
            }
            {
                //dummy
                GroupEntryData dummyData = new GroupEntryData();
                dummyData.type = GroupEntryType.DUMMY;
                adapter.addValue(dummyData);
                try {
                    requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                } catch (Throwable e) {
                    //ignore
                }
            }
        } else {
            try {
                requireActivity().runOnUiThread(() -> binding.noGroupEntryDeclarationView.setVisibility(View.VISIBLE));
            } catch (Throwable e) {
                //ignore
            }
        }
    }


    @Override
    public void showLoading() {
        Utils.log("Show Loading");
        try {
            requireActivity().runOnUiThread(() -> binding.loadingNavView.setVisibility(View.VISIBLE));
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    public void dismissLoading() {
        try {
            requireActivity().runOnUiThread(() -> binding.loadingNavView.setVisibility(View.GONE));
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    public void updateLoadingText(String text) {
        try {
            requireActivity().runOnUiThread(() -> {
                binding.loadingTextView.setText(text);
                if (binding.loadingTextView.getVisibility() == View.GONE) {
                    binding.loadingNavView.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            //ignore
        }
    }


    public void setGroup(UUID gId) {
        Utils.log("currentGid " + Db.getInstance().getCurrentGroupId() + " gId " + gId);
        shutDownExecutor();
        Db.getInstance().setCurrentGroupId(gId);
        show(gId, GroupsAndEntriesAction.ALL, null);
    }

    public void showAllEntryOnly() {
        shutDownExecutor();
        show(Db.getInstance().getCurrentGroupId(), GroupsAndEntriesAction.ALL_ENTRIES_ONLY, null);
    }

    public void showAllEntryOnly(String query) {
        shutDownExecutor();
        show(Db.getInstance().getCurrentGroupId(), GroupsAndEntriesAction.ALL_ENTRIES_ONLY, query);
    }

}
