package org.j_keepass.list_group_and_entry.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.ListGroupsAndEntriesActivityLayoutBinding;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.changeactivity.ChangeActivityEvent;
import org.j_keepass.events.changeactivity.ChangeActivityEventSource;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.newpwd.GenerateNewPwdEvent;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionResultEvent;
import org.j_keepass.events.permission.PermissionResultEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.events.themes.ThemeEvent;
import org.j_keepass.fields.activities.FieldActivity;
import org.j_keepass.list_db.activities.ListDbActivity;
import org.j_keepass.list_db.bsd.BsdUtil;
import org.j_keepass.list_db.util.themes.SetTheme;
import org.j_keepass.list_db.util.themes.Theme;
import org.j_keepass.list_group_and_entry.fragments.ListGroupsAndEntriesFragment;
import org.j_keepass.stats.fragments.StatsFragment;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListGroupAndEntriesActivity extends AppCompatActivity implements ThemeEvent, GenerateNewPwdEvent, ChangeActivityEvent, ReloadEvent, PermissionResultEvent {
    private ListGroupsAndEntriesActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;

    private ActivityResultLauncher<Intent> exportLauncher = null;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = ListGroupsAndEntriesActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        exportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onActivityResult(result.getResultCode(), result.getData()));
        configureBackPressed();
        register();
        ExecutorService executor = getExecutor();
        executor.execute(this::configureClicks);
        executor.execute(new SleepFor1Ms());
        executor.execute(this::configureTabLayout);
        executor.execute(this::addTabs);
    }

    private void configureClicks() {
        binding.lockBtn.setOnClickListener(view -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.locking));
                LoadingEventSource.getInstance().showLoading();
                Utils.sleepFor3MSec();
                Db.getInstance().deSetDatabase();
                ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityAction.LOCK);
            });
        });
        binding.groupAndEntryHomeDatabaseBtn.setOnClickListener(view -> {
            TabLayout.Tab selectedTab = binding.groupAndEntryTabLayout.getTabAt(0);
            if (selectedTab != null) {
                selectedTab.select();
                setGroup(Db.getInstance().getRootGroupId());
            }
        });
        binding.groupAndEntryGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.generatingNewPwd));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
        });
        binding.groupMoreOption.setOnClickListener(view -> showMenu(view.getContext()));
        binding.groupAndEntryAddNewBtn.setOnClickListener(view -> new org.j_keepass.list_group_and_entry.bsd.BsdUtil().showAskForAddNewItems(view.getContext(), this, Db.getInstance().getGroupName(Db.getInstance().getCurrentGroupId())));
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void register() {
        GenerateNewPasswordEventSource.getInstance().addListener(this);
        ChangeActivityEventSource.getInstance().addListener(this);
        ReloadEventSource.getInstance().addListener(this);
        PermissionResultEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        GenerateNewPasswordEventSource.getInstance().removeListener(this);
        ChangeActivityEventSource.getInstance().removeListener(this);
        ReloadEventSource.getInstance().removeListener(this);
        PermissionResultEventSource.getInstance().removeListener(this);
    }

    @Override
    protected void onDestroy() {
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

    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Utils.log("Applying theme " + theme.asString());
            setTheme(theme.getResId());
            AppCompatDelegate.setDefaultNightMode(theme.getMode());
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (theme.isLightTheme()) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                }
            } catch (Exception e) {
                Utils.log("Error setting light status " + e.getMessage());
            }
        });
    }

    private void configureTabLayout() {
        Utils.log("Configure Tab Layout");
        binding.groupAndEntryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Utils.log("Tab selected " + tab.getId());
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_list_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        ListGroupsAndEntriesFragment listGroupsAndEntriesFragment = new ListGroupsAndEntriesFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "showAll");
                        listGroupsAndEntriesFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, listGroupsAndEntriesFragment).commit();
                    }
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        binding.groupNameOnTop.setText(tab.view.getResources().getString(R.string.entries));
                        ListGroupsAndEntriesFragment listGroupsAndEntriesFragment = new ListGroupsAndEntriesFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "showEntryOnly");
                        listGroupsAndEntriesFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, listGroupsAndEntriesFragment).commit();
                    }
                } else if (tab.getId() == 2) {
                    tab.setIcon(R.drawable.ic_graph_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        binding.groupNameOnTop.setText(tab.view.getResources().getString(R.string.statistics));
                        StatsFragment statsFragment = new StatsFragment();
                        getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, statsFragment).commit();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.background_transparent);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
                } else if (tab.getId() == 2) {
                    tab.setIcon(R.drawable.ic_graph_fill0_wght300_grad_25_opsz24);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addTabs() {
        Utils.log("creating tab");
        int id = 0;
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.list);
            databaseTab.setIcon(R.drawable.ic_list_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            Utils.log("Added tab with id " + id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Utils.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.entries);
            databaseTab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(false);
            databaseTab.setId(id);
            Utils.log("Added tab with id " + id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Utils.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.groupAndEntryTabLayout.newTab();
            databaseTab.setText(R.string.statistics);
            databaseTab.setIcon(R.drawable.ic_graph_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(false);
            databaseTab.setId(id);
            Utils.log("Added tab with id " + id);
            id++;
            runOnUiThread(() -> {
                binding.groupAndEntryTabLayout.addTab(databaseTab, databaseTab.getId());
                Utils.log("Added tab");
            });
        }
        Utils.log("Added tab with max id " + id);
    }

    public void setGroup(UUID gId) {
        Db.getInstance().setCurrentGroupId(gId);
        binding.groupNameOnTop.setText(Db.getInstance().getGroupName(gId));
        ReloadEventSource.getInstance().reload(ReloadAction.GROUP_UPDATE);
    }

    @Override
    public void reload(ReloadAction reloadAction) {
        if (reloadAction != null && reloadAction.name().equals(ReloadAction.GROUP_UPDATE.name())) {
            try {
                runOnUiThread(() -> binding.groupNameOnTop.setText(Db.getInstance().getGroupName(Db.getInstance().getCurrentGroupId())));
            } catch (Throwable t) {
                // ignore
            }
        } else if (reloadAction != null && reloadAction.name().equals(ReloadAction.NAV_GROUP.name())) {
            try {
                runOnUiThread(() -> {
                    TabLayout.Tab selectedTab = binding.groupAndEntryTabLayout.getTabAt(0);
                    if (selectedTab != null) {
                        selectedTab.select();
                        shutDownExecutor();
                        if (!isFinishing() && !isDestroyed()) {
                            ListGroupsAndEntriesFragment listGroupsAndEntriesFragment = new ListGroupsAndEntriesFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("show", "showAll");
                            listGroupsAndEntriesFragment.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.groupAndEntryFragmentContainerView, listGroupsAndEntriesFragment).commit();
                        }
                    }
                });
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    @Override
    public void changeActivity(ChangeActivityAction changeActivityAction) {
        if (changeActivityAction != null && changeActivityAction.name().equals(ChangeActivityAction.ENTRY_SELECTED.name())) {
            Intent intent = new Intent(this, FieldActivity.class);
            intent.putExtra("isEdit", false);
            intent.putExtra("isNew", false);
            startActivity(intent);
            finish();
        }
        if (changeActivityAction != null && changeActivityAction.name().equals(ChangeActivityAction.ENTRY_NEW.name())) {
            Intent intent = new Intent(this, FieldActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("isNew", true);
            startActivity(intent);
            finish();
        }
        if (changeActivityAction != null && changeActivityAction.name().equals(ChangeActivityAction.LOCK.name())) {
            Db.getInstance().deSetDatabase();
            Intent intent = new Intent(this, ListDbActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void configureBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Utils.log("On back selected tab pos is " + binding.groupAndEntryTabLayout.getSelectedTabPosition());
                if (binding.groupAndEntryTabLayout.getSelectedTabPosition() == 0) {
                    if (Db.getInstance().getRootGroupId().equals(Db.getInstance().getCurrentGroupId())) {
                        ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityAction.LOCK);
                    } else {
                        setGroup(Db.getInstance().getParentGroupId(Db.getInstance().getCurrentGroupId()));
                    }
                } else {
                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.NAV_GROUP);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
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

    public void showMenu(Context context) {
        new org.j_keepass.list_group_and_entry.bsd.BsdUtil().showGroupEntryMoreOptionsMenu(context, this, Db.getInstance().getGroupName(Db.getInstance().getCurrentGroupId()));
    }

    @Override
    public void permissionDenied(PermissionEvent.PermissionAction permissionAction) {
        Utils.log("Landing Permission Not Granted");
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.permissionNotGranted));
            LoadingEventSource.getInstance().showLoading();
        });
    }

    @Override
    public void permissionGranted(PermissionEvent.PermissionAction permissionAction) {
        if (permissionAction != null && permissionAction.name().equals(PermissionEvent.PermissionAction.EXPORT.name())) {
            Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            chooseFile.putExtra(Intent.EXTRA_TITLE, new File(Db.getInstance().getDbName()).getName());

            chooseFile = Intent.createChooser(chooseFile, "Choose a folder");
            exportLauncher.launch(chooseFile);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.log("requestCode " + requestCode);
        if (requestCode == PICK_FOLDER_OPEN_RESULT_CODE) {
            if (resultCode == -1) {
                ExecutorService executor = getExecutor();
                executor.execute(() -> exportDb(data.getData()));
            }
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        if (resultCode == -1) {
            ExecutorService executor = getExecutor();
            executor.execute(() -> exportDb(data.getData()));
        }
    }

    private void exportDb(Uri dataUri) {
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.exporting));
            LoadingEventSource.getInstance().showLoading();
        });
        executor.execute(() -> Db.getInstance().exportFile(dataUri, getContentResolver(), this));
        executor.execute(() -> LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.done)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Utils.log("Permission requestCode " + requestCode);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            PermissionResultEventSource.getInstance().permissionGranted(PermissionEvent.PermissionAction.EXPORT);
        } else {
            PermissionResultEventSource.getInstance().permissionDenied(PermissionEvent.PermissionAction.EXPORT);
        }
    }
}
