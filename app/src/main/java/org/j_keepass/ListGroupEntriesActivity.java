package org.j_keepass;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.databinding.ListGroupEntryActivityLayoutBinding;
import org.j_keepass.fragments.listgroupentry.ListGroupEntryFragment;
import org.j_keepass.groupentry.eventinterface.GroupEntryEvent;
import org.j_keepass.groupentry.eventinterface.GroupEntryEventSource;
import org.j_keepass.listgroupentry.eventinterface.MoreOptionEventSource;
import org.j_keepass.listgroupentry.eventinterface.MoreOptionsEvent;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.newpwd.eventinterface.GenerateNewPasswordEventSource;
import org.j_keepass.newpwd.eventinterface.GenerateNewPwdEvent;
import org.j_keepass.theme.eventinterface.ThemeEvent;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Util;
import org.j_keepass.util.bsd.landing.BsdUtil;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.theme.SetTheme;
import org.j_keepass.util.theme.Theme;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListGroupEntriesActivity extends AppCompatActivity implements ThemeEvent, GroupEntryEvent, GenerateNewPwdEvent, MoreOptionsEvent {
    private ListGroupEntryActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = ListGroupEntryActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        configureBackPressed();
        register();
        ExecutorService executor = getExecutor();
        executor.execute(this::configureClicks);
        executor.execute(new SleepFor1Ms());
        executor.execute(this::configureTabLayout);
        executor.execute(this::addTabs);
    }

    private void configureClicks() {
        binding.lockBtn.setOnClickListener(view -> GroupEntryEventSource.getInstance().lock());
        binding.groupAndEntryHomeDatabaseBtn.setOnClickListener(view -> {
            binding.groupAndEntryTabLayout.getTabAt(0).select();
            GroupEntryEventSource.getInstance().setGroup(Db.getInstance().getRootGroupId());
        });
        binding.groupAndEntryGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(getString(R.string.generatingNewPassword));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
        });
        binding.groupMoreOption.setOnClickListener(view -> MoreOptionEventSource.getInstance().showMenu(view.getContext()));
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void register() {
        GenerateNewPasswordEventSource.getInstance().addListener(this);
        GroupEntryEventSource.getInstance().addListener(this);
        MoreOptionEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        GenerateNewPasswordEventSource.getInstance().removeListener(this);
        GroupEntryEventSource.getInstance().removeListener(this);
        MoreOptionEventSource.getInstance().removeListener(this);
    }

    @Override
    protected void onDestroy() {
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
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_list_fill1_wght300_grad_25_opsz24);
                    tab.setId(-1);
                    if (!isFinishing() && !isDestroyed()) {
                        ListGroupEntryFragment listGroupEntryFragment = new ListGroupEntryFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, listGroupEntryFragment).commit();
                    }
                } else if (tab.getId() == -1) {
                    tab.setIcon(R.drawable.ic_list_fill1_wght300_grad_25_opsz24);
                    GroupEntryEventSource.getInstance().showAll();
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
                    GroupEntryEventSource.getInstance().showAllEntryOnly();
                } else if (tab.getId() == 3) {
                    tab.setIcon(R.drawable.ic_graph_fill1_wght300_grad_25_opsz24);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.background_transparent);
                if (tab.getId() == 0 || tab.getId() == -1) {
                    tab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
                } else if (tab.getId() == 3) {
                    tab.setIcon(R.drawable.ic_graph_fill0_wght300_grad_25_opsz24);
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
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.list);
            databaseTab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
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
            databaseTab.setText(R.string.entries);
            databaseTab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
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
            databaseTab.setText(R.string.statistics);
            databaseTab.setIcon(R.drawable.ic_graph_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(false);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Util.log("Added tab");
            });
        }
    }

    @Override
    public void setGroup(UUID gId) {
        Db.getInstance().setCurrentGroupId(gId);
        runOnUiThread(() -> binding.groupNameOnTop.setText(Db.getInstance().getGroupName(gId)));
    }

    private void configureBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (Db.getInstance().getRootGroupId().equals(Db.getInstance().getCurrentGroupId())) {
                    finish();
                } else {
                    GroupEntryEventSource.getInstance().setGroup(Db.getInstance().getParentGroupId(Db.getInstance().getCurrentGroupId()));
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void lock() {
        Db.getInstance().deSetDatabase();
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(getString(R.string.locking));
            LoadingEventSource.getInstance().showLoading();
            Util.sleepFor3MSec();
        });
        executor.execute(() -> runOnUiThread(() -> {
            Intent intent = new Intent(this, LandingAndListDatabaseActivity.class);
            startActivity(intent);
            finish();
        }));
    }

    @Override
    public void showAllEntryOnly() {
        //ignore
    }

    @Override
    public void showAllEntryOnly(String query) {
        // ignore
    }

    @Override
    public void showAll() {
        //ignore
    }

    @Override
    public void generateNewPwd(boolean useDigit, boolean useUpperCase, boolean useLowerCase, boolean useSymbol, int length) {
        //ignore
    }

    @Override
    public void generateNewPwd() {
        //ignore
    }

    @Override
    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        runOnUiThread(() -> {
            new BsdUtil().newPwdBsd(binding.getRoot().getContext(), newPwd, useDigit, useLowerCase, useUpperCase, useSymbol, length);
            LoadingEventSource.getInstance().dismissLoading();
        });
    }

    @Override
    public void showFailedNewGenPwd(String errorMsg) {
        runOnUiThread(() -> {
            LoadingEventSource.getInstance().updateLoadingText(errorMsg);
            LoadingEventSource.getInstance().showLoading();
        });
    }

    @Override
    public void showMenu(Context context) {
        new org.j_keepass.util.bsd.groupentry.BsdUtil().showGroupEntryMoreOptionsMenu(context, this, Db.getInstance().getGroupName(Db.getInstance().getCurrentGroupId()));
    }

}
