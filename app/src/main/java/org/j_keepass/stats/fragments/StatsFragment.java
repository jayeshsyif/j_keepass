package org.j_keepass.stats.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.j_keepass.R;
import org.j_keepass.databinding.StatsFragmentBinding;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.loading.LoadingEvent;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.stats.fragments.graph.PieChartView;
import org.j_keepass.util.Utils;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsFragment extends Fragment implements LoadingEvent {
    private StatsFragmentBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.log("List db frag on create");
        super.onCreate(savedInstanceState);
        binding = StatsFragmentBinding.inflate(getLayoutInflater());
    }

    private void register() {
        LoadingEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        LoadingEventSource.getInstance().removeListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.log("List graph frag on create view");
        View view = binding.getRoot();
        register();
        ExecutorService executor = getExecutor();
        executor.execute(() -> updateLoadingText(view.getContext().getString(R.string.loading)));
        executor.execute(this::showLoading);
        executor.execute(this::loadStat);
        executor.execute(this::dismissLoading);
        return view;
    }

    private void loadStat() {
        ArrayList<Float> values = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        final long totalEntries = Db.getInstance().getAllEntriesCount();
        final long allExpiredEntriesCount = Db.getInstance().getAllExpiredEntriesCount();
        final long allExpiringSoonEntriesCount = Db.getInstance().getAllExpiringSoonEntriesCount();
        final long ok = totalEntries - (allExpiringSoonEntriesCount + allExpiredEntriesCount);
        try {
            requireActivity().runOnUiThread(() -> {
                binding.expiredCountDisplay.setText(String.valueOf(allExpiredEntriesCount));
                binding.expiringSoonCountDisplay.setText(String.valueOf(allExpiringSoonEntriesCount));
                binding.goodCountDisplay.setText(String.valueOf(ok));
            });
        } catch (Throwable e) {
            //ignore
        }
        if (allExpiredEntriesCount > 0) {
            values.add((float) allExpiredEntriesCount);
            colors.add(binding.graph.getResources().getColor(R.color.kp_red));
            text.add("Expired " + allExpiredEntriesCount);
        }
        if (allExpiringSoonEntriesCount > 0) {
            values.add((float) allExpiringSoonEntriesCount);
            colors.add(binding.graph.getResources().getColor(R.color.kp_coral));
            text.add("Expiring Soon " + allExpiringSoonEntriesCount);
        }
        if (ok > 0) {
            values.add((float) ok);
            colors.add(binding.graph.getResources().getColor(R.color.kp_green));
            text.add("Good " + ok);
        }
        Utils.log("Total " + totalEntries);
        Utils.log("Expired " + allExpiredEntriesCount);
        Utils.log("Expiring Soon " + allExpiringSoonEntriesCount);
        Utils.log("Good " + ok);

        int textColor = binding.graph.getResources().getColor(R.color.kp_static_white);
        float textSize = 30;

        colors.add(binding.graph.getResources().getColor(R.color.kp_green));
        try {
            PieChartView pieChartView = new PieChartView(binding.graph.getContext(), values, colors, text, textColor, textSize);
            requireActivity().runOnUiThread(() -> binding.graph.addView(pieChartView));
        } catch (Throwable e) {
            //ignore
        }
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
}
