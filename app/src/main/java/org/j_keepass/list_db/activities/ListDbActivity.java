package org.j_keepass.list_db.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.ListDbActivityLayoutBinding;
import org.j_keepass.db.operation.DbAndFileOperations;
import org.j_keepass.db.events.DbEvent;
import org.j_keepass.db.events.DbEventSource;
import org.j_keepass.db.operation.Db;
import org.j_keepass.db.util.DummyDbDataUtil;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.newpwd.GenerateNewPwdEvent;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionEventSource;
import org.j_keepass.events.permission.PermissionResultEvent;
import org.j_keepass.events.permission.PermissionResultEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.events.themes.ThemeEvent;
import org.j_keepass.events.themes.ThemeEventSource;
import org.j_keepass.list_db.bsd.BsdUtil;
import org.j_keepass.list_db.fragments.ListDbFragment;
import org.j_keepass.list_db.util.themes.SetTheme;
import org.j_keepass.list_db.util.themes.Theme;
import org.j_keepass.list_group_and_entry.activities.ListGroupAndEntriesActivity;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ListDbActivity extends AppCompatActivity implements ThemeEvent, DbEvent, PermissionResultEvent, GenerateNewPwdEvent {
    private ListDbActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    private ActivityResultLauncher<Intent> importLauncher = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, true).run();
        binding = ListDbActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onActivityResult(result.getResultCode(), result.getData()));
        register();
        ExecutorService executor = getExecutor();
        executor.execute(this::configurePaths);
        executor.execute(() -> PermissionEventSource.getInstance().checkAndGetPermissionAlarm(binding.getRoot(), this, PermissionEvent.PermissionAction.ALARM));
        executor.execute(new SleepFor1Ms());
        executor.execute(this::configureClicks);
        executor.execute(this::configureTabLayout);
        executor.execute(this::addTabs);
    }

    private void register() {
        GenerateNewPasswordEventSource.getInstance().addListener(this);
        ThemeEventSource.getInstance().addListener(this);
        DbEventSource.getInstance().addListener(this);
        PermissionResultEventSource.getInstance().addListener(this);
    }

    private ExecutorService getExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executorServices.add(executor);
        return executor;
    }

    private void shutDownExecutor() {
        Utils.log("shutdown all");
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    private void configurePaths() {
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            Db.getInstance().setAppDirPath(new DbAndFileOperations().getDir(this));
            Db.getInstance().setAppSubDir(new DbAndFileOperations().getSubDir(this));
            new DbAndFileOperations().createMainDirectory(Db.getInstance().getAppDirPath());
            new DbAndFileOperations().createSubFilesDirectory(Db.getInstance().getAppSubDir());
        });
    }

    private void configureClicks() {
        binding.landingMoreOption.setOnClickListener(view -> showMenu(view.getContext()));
        binding.landingCreateDatabaseBtn.setOnClickListener(view -> new BsdUtil().showCreateDbBsd(view.getContext()));
        binding.landingGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.generatingNewPwd));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
        });
        binding.landingInfoBtn.setOnClickListener(view -> showInfo(view.getContext()));
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

    private void unregister() {
        ThemeEventSource.getInstance().removeListener(this);
        DbEventSource.getInstance().removeListener(this);
        PermissionResultEventSource.getInstance().removeListener(this);
        GenerateNewPasswordEventSource.getInstance().removeListener(this);
    }

    private void configureTabLayout() {
        Utils.log("Configure Tab Layout");
        binding.landingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_database_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        Utils.log("Tab selected, frag counts are " + getSupportFragmentManager().getFragments().size());
                        ListDbFragment listDbFragment = new ListDbFragment();
                        if (getIntent() != null && getIntent().getData() != null) {
                            Uri data = getIntent().getData();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("openFileData", data);
                            listDbFragment.setArguments(bundle);
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.landingFragmentContainerView, listDbFragment).commit();
                        Utils.log("Tab selected done");
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
        Utils.log("creating tab");
        TabLayout.Tab databaseTab = binding.landingTabLayout.newTab();
        databaseTab.setText(R.string.db);
        databaseTab.setIcon(R.drawable.ic_database_fill0_wght300_grad_25_opsz24);
        databaseTab.view.setSelected(true);
        databaseTab.setId(0);
        runOnUiThread(() -> {
            binding.landingTabLayout.addTab(databaseTab, 0);
            Utils.log("Added tab");
        });
    }


    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Utils.log("Applying theme " + theme.asString());
            setTheme(theme.getResId());
            AppCompatDelegate.setDefaultNightMode(theme.getMode());
            try {
                if (theme.isLightTheme()) {
                    setSystemBarLight(this);
                }
            } catch (Exception e) {
                Utils.log("Error setting light status " + e.getMessage());
            }
            if (updatePref) {
                updateThemeInPref(theme.getId());
            }
        });
    }

    public void updateThemeInPref(String id) {
        Utils.log("Updating theme in pref");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sTheme", id);
        editor.apply();
        finish();
        startActivity(new Intent(this, this.getClass()));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void showMenu(Context context) {
        Utils.log("Show menu via listener");
        new BsdUtil().showLandingMoreOptionsMenu(context, this);
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
    public void showFailedNewGenPwd(final String errorMsg) {
        runOnUiThread(() -> {
            LoadingEventSource.getInstance().updateLoadingText(errorMsg);
            LoadingEventSource.getInstance().showLoading();
        });
    }

    public void showInfo(Context context) {
        new BsdUtil().showInfo(context);
    }

    @Override
    public void createDb(final String dbName, final String pwd) {
        AtomicReference<String> dbNameAr = new AtomicReference<>(dbName);
        AtomicReference<File> newDbFile = new AtomicReference<>();
        AtomicBoolean proceed = new AtomicBoolean(true);
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.creatingNewDb));
            LoadingEventSource.getInstance().showLoading();
        });
        executor.execute(() -> {
            if (dbNameAr.get() == null || dbNameAr.get().isEmpty()) {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.enterDbName));
                proceed.set(false);
            } else if (pwd == null || pwd.isEmpty()) {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.enterPwd));
                proceed.set(false);
            } else if (!dbName.endsWith("kdbx")) {
                dbNameAr.set(dbName + ".kdbx");
            }
        });
        executor.execute(() -> {
            if (proceed.get()) {
                new DbAndFileOperations().createMainDirectory(Db.getInstance().getAppDirPath());
            }
            if (proceed.get()) {
                new DbAndFileOperations().createSubFilesDirectory(Db.getInstance().getAppSubDir());
            }
            if (proceed.get()) {
                proceed.set(Utils.checkCodecAvailable());
            }
            if (proceed.get()) {
                newDbFile.set(new DbAndFileOperations().createFile(Db.getInstance().getAppSubDir(), dbNameAr.get()));
            }
            if (proceed.get()) {
                new DbAndFileOperations().writeDbToFile(newDbFile.get(), pwd.getBytes(StandardCharsets.UTF_8), getContentResolver(), new DummyDbDataUtil().getDummyDatabase());
            }
            if (proceed.get()) {
                LoadingEventSource.getInstance().dismissLoading();
            } else {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.devInProgress));
            }
            if (proceed.get()) {
                ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.CREATE_NEW);
            }
        });
    }

    @Override
    public void openingDb() {
        shutDownExecutor();
    }

    @Override
    public void askPwdForDb(Context context, String dbName, String fullPath) {
        Utils.log("ask for db");
        try {
            new BsdUtil().showAskPwdForDb(context, dbName, fullPath);
        } catch (Throwable e) {
            //ignore
        }
    }

    @Override
    public void askPwdForDb(Context context, String dbName, Uri data) {
        Utils.log("ask for db");
        try {
            new BsdUtil().showAskPwdForDb(context, dbName, data);
        } catch (Throwable e) {
            //ignore
        }
    }

    @Override
    public void failedToOpenDb(String errorMsg) {
        LoadingEventSource.getInstance().updateLoadingText(errorMsg);
        LoadingEventSource.getInstance().showLoading();
    }

    @Override
    public void loadSuccessDb() {
        Utils.log(" load success Db");
        Intent intent = new Intent(this, ListGroupAndEntriesActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void permissionDenied(PermissionEvent.PermissionAction permissionAction) {
        Utils.log("Landing Permission Not Granted for action " + permissionAction.name());
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.permissionNotGranted));
            LoadingEventSource.getInstance().showLoading();
        });
    }

    @Override
    public void permissionGranted(PermissionEvent.PermissionAction permissionAction) {
        if (permissionAction != null && permissionAction.name().equals(PermissionEvent.PermissionAction.IMPORT.name())) {
            Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            chooseFile.setType("application/octet-stream");
            chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            importLauncher.launch(chooseFile);
        } else if (permissionAction != null && permissionAction.name().equals(PermissionEvent.PermissionAction.ALARM.name())) {
            Utils.log("Landing Alarm Permission Granted, setting notification");
            new org.j_keepass.notification.Util().startAlarmBroadcastReceiver(binding.getRoot().getContext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Utils.log("Permission requestCode " + requestCode);
        int READ_EXTERNAL_STORAGE = 100;
        int ALARM = 101;
        if (requestCode == READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PermissionResultEventSource.getInstance().permissionGranted(PermissionEvent.PermissionAction.IMPORT);
            } else {
                PermissionResultEventSource.getInstance().permissionDenied(PermissionEvent.PermissionAction.IMPORT);
            }
        }
        if (requestCode == ALARM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PermissionResultEventSource.getInstance().permissionGranted(PermissionEvent.PermissionAction.ALARM);
            } else {
                PermissionResultEventSource.getInstance().permissionDenied(PermissionEvent.PermissionAction.ALARM);
            }
        }

    }

    public void onActivityResult(int resultCode, Intent data) {
        Utils.log("import result code is  " + resultCode);
        if (resultCode == -1) {
            ExecutorService executor = getExecutor();
            executor.execute(() -> importDb(data.getData()));
        }
    }

    private void importDb(Uri dataUri) {
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.importing));
            LoadingEventSource.getInstance().showLoading();
        });
        executor.execute(() -> new DbAndFileOperations().importFile(Db.getInstance().getAppSubDir(), dataUri, getContentResolver(), this));
        executor.execute(() -> ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.IMPORT));
    }

    public void setSystemBarLight(Activity activity) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For API level 30 and above
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                // Set light status bar
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For API level 23 to 29, fallback to old method
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }
}
