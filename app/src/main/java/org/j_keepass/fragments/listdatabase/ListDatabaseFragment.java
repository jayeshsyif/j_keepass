package org.j_keepass.fragments.listdatabase;

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
import org.j_keepass.adapter.ListDbsAdapter;
import org.j_keepass.databinding.ListAllDatabaseFragmentBinding;
import org.j_keepass.db.eventinterface.DbAndFileOperations;
import org.j_keepass.db.eventinterface.DbEvent;
import org.j_keepass.db.eventinterface.DbEventSource;
import org.j_keepass.loading.eventinterface.LoadingEvent;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.util.Util;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ListDatabaseFragment extends Fragment implements LoadingEvent, DbEvent {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    private ListAllDatabaseFragmentBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Util.log("List db frag on create view");
        View view = binding.getRoot();
        LoadingEventSource.getInstance().addListener(this);
        DbEventSource.getInstance().addListener(this);
        initExecutor();
        executor.execute(this::showDbs);
        return view;
    }

    private void showDbs() {
        AtomicReference<String> dirPath = new AtomicReference<>("");
        AtomicReference<String> subFilesDirPath = new AtomicReference<>("");
        AtomicReference<ListDbsAdapter> adapter = new AtomicReference<>();
        initExecutor();
        executor.execute(this::showLoading);
        executor.execute(() -> dirPath.set(new DbAndFileOperations().getDir(getActivity())));
        executor.execute(() -> subFilesDirPath.set(new DbAndFileOperations().getSubDir(getActivity())));
        executor.execute(() -> new DbAndFileOperations().createMainDirectory(dirPath.get()));
        executor.execute(() -> new DbAndFileOperations().createSubFilesDirectory(subFilesDirPath.get()));
        executor.execute(() -> adapter.set(configureRecyclerView(binding.showDbsRecyclerView.getContext())));
        executor.execute(() -> showDbs(subFilesDirPath.get(), adapter.get()));
        executor.execute(this::dismissLoading);
    }

    private ListDbsAdapter configureRecyclerView(Context context) {
        Util.log("Configuration recycler view");
        ListDbsAdapter adapter = new ListDbsAdapter();
        try {
            requireActivity().runOnUiThread(() -> {
                Util.log("Configuration recycler view inside ui thread");
                binding.showDbsRecyclerView.removeAllViews();
                binding.showDbsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                binding.showDbsRecyclerView.setAdapter(adapter);
                Util.log("Configuration recycler view done");
            });
        } catch (Exception e) {
            Util.log("Configuration recycler view, Error " + e.getMessage());
        }
        return adapter;
    }

    private void showDbs(String subFilesDirPath, ListDbsAdapter adapter) {
        if (adapter == null) {
            Util.log("Showing Db from " + subFilesDirPath + " Adapter is null");
        } else {
            Util.log("Showing Db from " + subFilesDirPath);
            File subFilesDir = new File(subFilesDirPath);
            File[] files = subFilesDir.listFiles();
            if (files != null && files.length > 0) {
                Arrays.sort(files);
                requireActivity().runOnUiThread(() -> binding.showDbsRecyclerView.setVisibility(View.VISIBLE));
                int fCount = 1;
                String loadingStr = getString(R.string.loading);
                for (File f : files) {
                    updateLoadingText(loadingStr + " [" + fCount + "/" + files.length + "]");
                    Util.log("Db is " + f.getName());
                    ListDbsAdapter.DbData d = adapter.getDbDatObj();
                    d.lastModified = f.lastModified();
                    d.dbName = f.getName();
                    adapter.addValue(d);
                    try {
                        requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
                    } catch (Exception e) {
                        //ignore
                    }
                    fCount++;
                    Util.sleepFor3MSec();
                }
                try {
                    requireActivity().runOnUiThread(() -> binding.dbDeclarationTextView.setText(R.string.declaration));
                } catch (Exception e) {
                    //ignore
                }
            } else {
                Util.log("No Dbs");
                try {
                    requireActivity().runOnUiThread(() -> binding.dbDeclarationTextView.setText(R.string.importOrCreateDb));
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Util.log("List db frag on create");
        super.onCreate(savedInstanceState);
        binding = ListAllDatabaseFragmentBinding.inflate(getLayoutInflater());
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
            requireActivity().runOnUiThread(() -> binding.loadingTextView.setText(text));
        } catch (Exception e) {
            //ignore
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Util.log("List frag destroy");
        LoadingEventSource.getInstance().removeListener(this);
        shutDownExecutor();
    }

    private void shutDownExecutor() {
        executor.shutdownNow();
    }
    private void initExecutor() {
        if (executor == null || executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void createDb(String name, String pwd) {
    }

    @Override
    public void reloadDbFile() {
        initExecutor();
        executor.execute(this::showDbs);
    }
}