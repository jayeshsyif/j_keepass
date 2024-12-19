package org.j_keepass.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.j_keepass.R;
import org.j_keepass.databinding.StatsFragmentBinding;
import org.j_keepass.loading.eventinterface.LoadingEvent;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.stats.PieChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsFragment extends Fragment implements LoadingEvent {
    private StatsFragmentBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Util.log("List db frag on create");
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
        Util.log("List graph frag on create view");
        View view = binding.getRoot();
        register();
        ExecutorService executor = getExecutor();
        executor.execute(() -> updateLoadingText(getString(R.string.loading)));
        executor.execute(this::showLoading);
        executor.execute(this::loadStat);
        executor.execute(this::dismissLoading);
        return view;
    }

    private void loadStat() {
        ArrayList<Float> values = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        final long totalEntries = Db.getInstance().getAllExpiredEntriesCount();
        final long allExpiredEntriesCount = totalEntries;
        final long allExpiringSoonEntriesCount = Db.getInstance().getAllExpiringSoonEntriesCount();
        final long ok = Db.getInstance().getAllEntriesCount() - (allExpiringSoonEntriesCount + allExpiredEntriesCount);
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
            values.add(Float.valueOf(allExpiredEntriesCount));
            colors.add(binding.graph.getResources().getColor(android.R.color.holo_red_dark));
            text.add("Expired " + allExpiredEntriesCount);
        }
        if (allExpiringSoonEntriesCount > 0) {
            values.add(Float.valueOf(allExpiringSoonEntriesCount));
            colors.add(binding.graph.getResources().getColor(R.color.kp_coral));
            text.add("Expiring Soon " + allExpiringSoonEntriesCount);
        }
        if (ok > 0) {
            values.add(Float.valueOf(ok));
            colors.add(binding.graph.getResources().getColor(R.color.kp_green));
            text.add("Good " + ok);
        }
        Util.log("Total " + totalEntries);
        Util.log("Expired " + allExpiredEntriesCount);
        Util.log("Expiring Soon " + allExpiringSoonEntriesCount);
        Util.log("Good " + ok);

        int textColor = getSecondaryColor(binding.graph.getContext());
        float textSize = 30;

        colors.add(binding.graph.getResources().getColor(R.color.kp_green));
        try {
            PieChartView pieChartView = new PieChartView(binding.graph.getContext(), values, colors, text, textColor, textSize);
            requireActivity().runOnUiThread(() -> {
                binding.graph.addView(pieChartView);
            });
        } catch (Throwable e) {
            //ignore
        }
    }

    public static int getSecondaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean resolved = theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);

        if (resolved) {
            return typedValue.resourceId != 0 ? ContextCompat.getColor(context, typedValue.resourceId) : typedValue.data;
        }
        return ContextCompat.getColor(context, R.color.kp_static_dark_blue); // Replace with your fallback color
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
        Util.log("Graph frag destroy");
        unregister();
        destroy();
    }

    private void destroy() {
        shutDownExecutor();
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
}
