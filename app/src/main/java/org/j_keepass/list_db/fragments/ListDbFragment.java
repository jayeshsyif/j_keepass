package org.j_keepass.list_db.fragments;

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
import org.j_keepass.databinding.ListDbFragmentBinding;
import org.j_keepass.db.events.DbAndFileOperations;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.loading.LoadingEvent;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.adapters.ListDbAdapter;
import org.j_keepass.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ListDbFragment extends Fragment implements LoadingEvent, ReloadEvent {
    ArrayList<ExecutorService> executorServices = new ArrayList<>();
    private ListDbFragmentBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.log("List db frag on create");
        super.onCreate(savedInstanceState);
        binding = ListDbFragmentBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.log("List db frag on create view");
        View view = binding.getRoot();
        register();
        ExecutorService executor = getExecutor();
        executor.execute(this::showDbs);
        return view;
    }

    private void register() {
        LoadingEventSource.getInstance().addListener(this);
        ReloadEventSource.getInstance().addListener(this);
    }

    private void showDbs() {
        AtomicReference<ListDbAdapter> adapter = new AtomicReference<>();
        ExecutorService executor = getExecutor();
        executor.execute(this::showLoading);
        executor.execute(() -> new DbAndFileOperations().createMainDirectory(Db.getInstance().getAppDirPath()));
        executor.execute(() -> new DbAndFileOperations().createSubFilesDirectory(Db.getInstance().getAppSubDir()));
        executor.execute(() -> adapter.set(configureRecyclerView(binding.showDbsRecyclerView.getContext())));
        executor.execute(() -> showDbs(Db.getInstance().getAppSubDir(), adapter.get()));
        executor.execute(this::dismissLoading);
    }

    private ListDbAdapter configureRecyclerView(Context context) {
        Utils.log("Configuration recycler view");
        ListDbAdapter adapter = new ListDbAdapter();
        try {
            requireActivity().runOnUiThread(() -> {
                Utils.log("Configuration recycler view inside ui thread");
                binding.showDbsRecyclerView.removeAllViews();
                binding.showDbsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.showDbsRecyclerView.setAdapter(adapter);
                Utils.log("Configuration recycler view done");
            });
        } catch (Exception e) {
            Utils.log("Configuration recycler view, Error " + e.getMessage());
        }
        return adapter;
    }

    private void showDbs(String subFilesDirPath, ListDbAdapter adapter) {
        if (adapter == null) {
            Utils.log("Showing Db from " + subFilesDirPath + " Adapter is null");
        } else {
            Utils.log("Showing Db from " + subFilesDirPath);
            File subFilesDir = new File(subFilesDirPath);
            File[] files = subFilesDir.listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files);
                requireActivity().runOnUiThread(() -> {
                    binding.showDbsRecyclerView.setVisibility(View.VISIBLE);
                    binding.dbDeclarationTextView.setText(R.string.declaration);
                });
                int fCount = 1;
                String loadingStr = getString(R.string.loading);
                for (File f : files) {
                    updateLoadingText(loadingStr + " [" + fCount + "/" + files.length + "]");
                    Utils.log("Db is " + f.getName());
                    ListDbAdapter.DbData d = adapter.getDbDatObj();
                    d.lastModified = f.lastModified();
                    d.dbName = f.getName();
                    d.fullPath = f.getAbsolutePath();
                    adapter.addValue(d);
                    try {
                        requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                    } catch (Exception e) {
                        //ignore
                    }
                    Utils.sleepFor3MSec();
                    fCount++;
                }
                try {
                    ListDbAdapter.DbData d = adapter.getDbDatObj();
                    d.lastModified = -1;
                    d.dbName = getString(R.string.declaration);
                    d.fullPath = "";
                    Utils.log(d.toString());
                    adapter.addValue(d);
                    try {
                        requireActivity().runOnUiThread(() -> adapter.notifyItemInserted(adapter.getItemCount()));
                    } catch (Exception e) {
                        //ignore
                    }
                } catch (Exception e) {
                    //ignore
                }
            } else {
                Utils.log("No Dbs");
                try {
                    requireActivity().runOnUiThread(() -> binding.dbDeclarationTextView.setText(R.string.importOrCreateDb));
                } catch (Exception e) {
                    //ignore
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.log("List frag destroy");
        destroy();
    }

    private void destroy() {
        unregister();
        shutDownExecutor();
    }

    private void unregister() {
        LoadingEventSource.getInstance().removeListener(this);
        ReloadEventSource.getInstance().removeListener(this);
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
    public void reload(ReloadAction reloadAction) {
        ArrayList<String> supportedActions = new ArrayList<>();
        supportedActions.add(ReloadAction.EDIT.name());
        supportedActions.add(ReloadAction.CREATE_NEW.name());
        supportedActions.add(ReloadAction.IMPORT.name());
        supportedActions.add(ReloadAction.DELETE.name());
        if (reloadAction != null && (supportedActions.contains(reloadAction.name()))) {
            ExecutorService executor = getExecutor();
            executor.execute(this::showDbs);
        }
    }
}
