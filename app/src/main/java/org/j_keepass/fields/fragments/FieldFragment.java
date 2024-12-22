package org.j_keepass.fields.fragments;

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
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.fields.adapters.ListFieldAdapter;
import org.j_keepass.databinding.FieldFragmentBinding;
import org.j_keepass.fields.bsd.BsdUtil;
import org.j_keepass.fields.dtos.FieldData;
import org.j_keepass.fields.enums.FieldNameType;
import org.j_keepass.fields.enums.FieldValueType;
import org.j_keepass.events.loading.LoadingEvent;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.util.Utils;
import org.j_keepass.db.operation.Db;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class FieldFragment extends Fragment implements LoadingEvent, ReloadEvent {

    private FieldFragmentBinding binding;

    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    String show = "base";
    Boolean isEdit = false;
    Boolean isNew = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.log("List entry base frag on create");
        super.onCreate(savedInstanceState);
        binding = FieldFragmentBinding.inflate(getLayoutInflater());
    }

    private void register() {
        LoadingEventSource.getInstance().addListener(this);
        ReloadEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        LoadingEventSource.getInstance().removeListener(this);
        ReloadEventSource.getInstance().removeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.log("List entry frag on create view");
        View view = binding.getRoot();
        register();
        Utils.log("Entry title is " + Db.getInstance().getEntryTitle(Db.getInstance().getCurrentEntryId()));
        Bundle bundle = getArguments();
        if (bundle != null) {
            String showBundle = bundle.getString("show");
            Boolean isEditBundle = bundle.getBoolean("isEdit");
            Boolean isNewBundle = bundle.getBoolean("isNew");
            if (showBundle != null && showBundle.length() > 0) {
                show = showBundle;
            }
            isEdit = isEditBundle;
            isNew = isNewBundle;
        }
        ExecutorService executor = getExecutor();
        executor.execute(this::showLoading);
        executor.execute(this::dismissLoading);
        executor.execute(() -> setAdapterAndShowFields(show, isEdit, isNew));
        return view;
    }

    private void setAdapterAndShowFields(final String show, final Boolean isEdit, final Boolean isNew) {
        ExecutorService executor = getExecutor();
        executor.execute(() -> updateLoadingText(binding.getRoot().getContext().getString(R.string.loading)));
        executor.execute(this::showLoading);
        AtomicReference<ListFieldAdapter> adapter = new AtomicReference<>();
        executor.execute(() -> adapter.set(configureRecyclerView(binding.entryFieldsRecyclerView.getContext(), isEdit)));
        executor.execute(() -> showFields(adapter.get(), show, isEdit, isNew));
        executor.execute(this::dismissLoading);
    }

    private void showFields(ListFieldAdapter adapter, final String show, final boolean isEdit, final Boolean isNew) {
        Utils.log("Entry show with " + show + " is edit as ");
        ArrayList<FieldData> fields;
        if (show.equals("additional")) {
            fields = Db.getInstance().getAdditionalFields(Db.getInstance().getCurrentEntryId(), isNew);
        } else if (show.equals("attachment")) {
            fields = Db.getInstance().getAttachments(Db.getInstance().getCurrentEntryId(), isNew);
        } else {
            //base
            fields = Db.getInstance().getFields(Db.getInstance().getCurrentEntryId(), isNew);
        }
        if (fields != null && fields.size() > 0) {
            requireActivity().runOnUiThread(() -> {
                binding.entryFieldsRecyclerView.setVisibility(View.VISIBLE);
                binding.entryTitleView.setVisibility(View.GONE);
            });
            for (final FieldData fd : fields) {
                adapter.addValue(fd);
                try {
                    requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                } catch (Throwable e) {
                    //ignore
                }
            }
            {
                //Dummy
                FieldData fd = new FieldData();
                fd.name = FieldNameType.DUMMY.toString();
                fd.value = "DUMMY";
                fd.fieldValueType = FieldValueType.DUMMY;
                fd.fieldNameType = FieldNameType.DUMMY;
                adapter.addValue(fd);
                try {
                    requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                } catch (Throwable e) {
                    //ignore
                }
            }
        } else if (isEdit || isNew) {
            try {
                requireActivity().runOnUiThread(() -> {
                    binding.addAdditionalPropertyBtn.setVisibility(View.VISIBLE);
                    binding.addAdditionalPropertyBtn.setOnClickListener(view -> {
                        new BsdUtil().showAddNewProperty(binding.addAdditionalPropertyBtn.getContext(), Db.getInstance().getCurrentEntryId());
                    });
                });
            } catch (Throwable e) {
                //ignore
            }
        }
    }

    private ListFieldAdapter configureRecyclerView(Context context, final Boolean isEdit) {
        Utils.log("Configuration recycler view");
        ListFieldAdapter adapter = new ListFieldAdapter();
        adapter.setEditable(isEdit);
        try {
            requireActivity().runOnUiThread(() -> {
                Utils.log("Configuration recycler view inside ui thread");
                binding.entryFieldsRecyclerView.removeAllViews();
                binding.entryFieldsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.entryFieldsRecyclerView.setAdapter(adapter);
                Utils.log("Configuration recycler view done");
            });
        } catch (Throwable e) {
            Utils.log("Configuration recycler view, Error " + e.getMessage());
        }
        return adapter;
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void shutDownExecutor() {
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("Graph frag destroy");
        unregister();
        destroy();
    }

    private void destroy() {
        shutDownExecutor();
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

    @Override
    public void reload(ReloadAction reloadAction) {
        if (reloadAction.name().equals(ReloadAction.ENTRY_ADDITIONAL_PROP_UPDATE.name().toString())) {
            ExecutorService executor = getExecutor();
            executor.execute(this::showLoading);
            executor.execute(this::dismissLoading);
            executor.execute(() -> setAdapterAndShowFields(show, isEdit, isNew));
        }
    }
}
