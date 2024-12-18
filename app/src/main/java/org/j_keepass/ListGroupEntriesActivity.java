package org.j_keepass;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.databinding.ListGroupEntryActivityLayoutBinding;
import org.j_keepass.fragments.listgroupentry.ListGroupEntryFragment;
import org.j_keepass.theme.eventinterface.ThemeEvent;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.theme.SetTheme;
import org.j_keepass.util.theme.Theme;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListGroupEntriesActivity extends AppCompatActivity implements ThemeEvent {
    private ListGroupEntryActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = ListGroupEntryActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.groupNameOnTop.setText(Db.getInstance().getRootGroupName());
        ExecutorService executor = getExecutor();
        executor.execute(new SleepFor1Ms());
        executor.execute(this::configureTabLayout);
        executor.execute(this::addTabs);
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.log("Landing destroy");
        destroy();
    }

    private void destroy() {
        Util.log("unregister");
        shutDownExecutor();
    }

    private void shutDownExecutor() {
        Util.log("shutdown all");
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Util.log("Applying theme " + theme.asString());
            setTheme(theme.getResId());
            AppCompatDelegate.setDefaultNightMode(theme.getMode());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (theme.isLightTheme()) {
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        }
                    }
                }
            } catch (Exception e) {
                Util.log("Error setting light status " + e.getMessage());
            }
        });
    }

    private void configureTabLayout() {
        Util.log("Configure Tab Layout");
        binding.groupAndEntryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Util.log("Tab selected " + tab.getId() + " " + tab.getText().toString());
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                if (tab.getId() == 0) {
                    if (!isFinishing() && !isDestroyed()) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, new ListGroupEntryFragment()).commit();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.background_transparent);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addTabs() {
        Util.log("creating tab");
        int id = 0;
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.all);
            databaseTab.setIcon(R.drawable.ic_database_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.groupFolder);
            databaseTab.setIcon(R.drawable.ic_database_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(false);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.entries);
            databaseTab.setIcon(R.drawable.ic_database_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(false);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
    }
}
