package org.j_keepass.fragments.listgroupentry;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.j_keepass.R;
import org.j_keepass.adapter.ListGroupEntryAdapter;
import org.j_keepass.databinding.ListAllGroupEntryFragmentBinding;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryData;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryType;
import org.j_keepass.groupentry.eventinterface.GroupEntryEvent;
import org.j_keepass.groupentry.eventinterface.GroupEntryEventSource;
import org.j_keepass.loading.eventinterface.LoadingEvent;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ListGroupEntryFragment extends Fragment implements LoadingEvent, GroupEntryEvent {
    ArrayList<ExecutorService> executorServices = new ArrayList<>();
    private ListAllGroupEntryFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Util.log("List db frag on create");
        super.onCreate(savedInstanceState);
        binding = ListAllGroupEntryFragmentBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Util.log("List group entry frag on create view");
        View view = binding.getRoot();
        register();
        GroupEntryEventSource.getInstance().setGroup(Db.getInstance().getRootGroupId());
        return view;
    }

    private void show(final UUID gId) {
        ExecutorService executor = getExecutor();
        AtomicReference<ListGroupEntryAdapter> adapter = new AtomicReference<>();
        executor.execute(() -> updateLoadingText(getString(R.string.loading)));
        executor.execute(this::showLoading);
        executor.execute(() -> adapter.set(configureRecyclerView(binding.showGroupEntriesRecyclerView.getContext())));
        executor.execute(() -> {
            if (Db.getInstance() != null && Db.getInstance().getRootGroupId() != null) {
                listFromGroupId(gId, adapter.get());
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
        GroupEntryEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        LoadingEventSource.getInstance().removeListener(this);
        GroupEntryEventSource.getInstance().removeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Util.log("Landing destroy");
        destroy();
    }

    private void destroy() {
        Util.log("unregister");
        unregister();
        shutDownExecutor();
    }

    private void shutDownExecutor() {
        Util.log("shutdown all");
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    private ListGroupEntryAdapter configureRecyclerView(Context context) {
        Util.log("Configuration recycler view");
        ListGroupEntryAdapter adapter = new ListGroupEntryAdapter();
        try {
            requireActivity().runOnUiThread(() -> {
                Util.log("Configuration recycler view inside ui thread");
                binding.showGroupEntriesRecyclerView.removeAllViews();
                binding.showGroupEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.showGroupEntriesRecyclerView.setAdapter(adapter);
                Util.log("Configuration recycler view done");
            });
        } catch (Throwable e) {
            Util.log("Configuration recycler view, Error " + e.getMessage());
        }
        return adapter;
    }

    private void listFromGroupId(UUID gId, ListGroupEntryAdapter adapter) {
        final int totalSubs = Db.getInstance().getSubGroupsCount(gId) + Db.getInstance().getSubEntriesCount(gId);
        if (totalSubs > 0) {
            try {
                requireActivity().runOnUiThread(() -> {
                    binding.showGroupEntriesRecyclerView.setVisibility(View.VISIBLE);
                    binding.noGroupEntryDeclarationView.setVisibility(View.GONE);
                });
            } catch (Throwable e) {
                //ignore
            }
            updateLoadingText(getString(R.string.loading) + " [0/" + totalSubs + "]");
            ArrayList<GroupEntryData> subs = Db.getInstance().getSubGroupsAndEntries(gId);
            Collections.sort(subs, (d1, d2) -> d1.name.compareTo(d2.name));
            int subCountAdded = 0;
            for (final GroupEntryData data : subs) {
                Util.log("Adding " + data.name);
                adapter.addValue(data);
                subCountAdded++;
                try {
                    requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                } catch (Throwable e) {
                    //ignore
                }
                updateLoadingText(getString(R.string.loading) + " [" + subCountAdded + "/" + totalSubs + "]");
                Util.sleepFor3MSec();
            }
            {
                //dummy
                GroupEntryData dummyData = new GroupEntryData();
                dummyData.type = GroupEntryType.DUMMY;
                adapter.addValue(dummyData);
                try {
                    requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
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
        Util.log("Show Loading");
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

    @Override
    public void setGroup(UUID gId) {
        shutDownExecutor();
        show(gId);
    }

    @Override
    public void lock() {
        //ignore
    }
}
