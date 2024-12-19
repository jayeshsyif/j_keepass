package org.j_keepass;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.databinding.FieldActivityLayoutBinding;
import org.j_keepass.fragments.entry.FieldFragment;
import org.j_keepass.groupentry.eventinterface.GroupEntryEventSource;
import org.j_keepass.theme.eventinterface.ThemeEvent;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.theme.SetTheme;
import org.j_keepass.util.theme.Theme;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FieldActivity extends AppCompatActivity implements ThemeEvent {
    private FieldActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = FieldActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        register();
        ExecutorService executor = getExecutor();
        executor.execute(new SleepFor1Ms());
        executor.execute(this::configureClicks);
        executor.execute(this::configureTabLayout);
        executor.execute(this::addTabs);
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void register() {

    }

    private void unregister() {

    }

    private void configureClicks() {
        binding.entryHomeBtn.setOnClickListener(view -> {
            GroupEntryEventSource.getInstance().setGroup(Db.getInstance().getRootGroupId());
            Intent intent = new Intent(this, ListGroupEntriesActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.log("Entry act destroy");
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

    private void configureTabLayout() {
        Util.log("Configure entry Tab Layout");
        binding.entryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Util.log("Tab selected " + tab.getId());
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        FieldFragment fieldFragment = new FieldFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "base");
                        fieldFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.entryFragmentContainerView, fieldFragment).commit();
                    }
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_list_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        FieldFragment fieldFragment = new FieldFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "additional");
                        fieldFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.entryFragmentContainerView, fieldFragment).commit();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.background_transparent);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
                }
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
            TabLayout.Tab databaseTab = binding.entryTabLayout.newTab();
            databaseTab.setText(R.string.entry);
            databaseTab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.entryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.entryTabLayout.newTab();
            databaseTab.setText(R.string.additionalDetails);
            databaseTab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.entryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
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
}
