package org.j_keepass;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.databinding.LandingAndListDatabaseActivityLayoutBinding;
import org.j_keepass.db.eventinterface.DbAndFileOperations;
import org.j_keepass.db.eventinterface.DbEvent;
import org.j_keepass.db.eventinterface.DbEventSource;
import org.j_keepass.fragments.listdatabase.ListDatabaseFragment;
import org.j_keepass.landing.eventinterface.MoreOptionEventSource;
import org.j_keepass.landing.eventinterface.MoreOptionsEvent;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.newpwd.eveninterface.GenerateNewPasswordEventSource;
import org.j_keepass.theme.eventinterface.ThemeEvent;
import org.j_keepass.theme.eventinterface.ThemeEventSource;
import org.j_keepass.util.bsd.BottomMenuUtil;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Util;
import org.j_keepass.util.theme.SetTheme;
import org.j_keepass.util.theme.Theme;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class LandingAndListDatabaseActivity extends AppCompatActivity implements MoreOptionsEvent, ThemeEvent, DbEvent {
    private LandingAndListDatabaseActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, true).run();
        binding = LandingAndListDatabaseActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        MoreOptionEventSource.getInstance().addListener(this);
        ThemeEventSource.getInstance().addListener(this);
        DbEventSource.getInstance().addListener(this);
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

    private void shutDownExecutor() {
        Util.log("shutdown all");
        for (ExecutorService executor : executorServices) {
            executor.shutdownNow();
        }
        executorServices = new ArrayList<>();
    }

    private void configureClicks() {
        binding.landingMoreOption.setOnClickListener(view -> MoreOptionEventSource.getInstance().showMenu(view.getContext()));
        binding.landingCreateDatabaseBtn.setOnClickListener(view -> MoreOptionEventSource.getInstance().showCreateNewDb(view.getContext()));
        binding.landingGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(getString(R.string.generatingNewPassword));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
        });
        binding.landingInfoBtn.setOnClickListener(view -> MoreOptionEventSource.getInstance().showInfo(view.getContext()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.log("Landing destroy");
        destroy();
    }

    private void destroy() {
        Util.log("unregister");
        MoreOptionEventSource.getInstance().removeListener(this);
        ThemeEventSource.getInstance().removeListener(this);
        DbEventSource.getInstance().removeListener(this);
        shutDownExecutor();
    }

    private void configureTabLayout() {
        Util.log("Configure Tab Layout");
        binding.landingTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (!isFinishing() && !isDestroyed()) {
                        getSupportFragmentManager().beginTransaction().remove(f).commit();
                    }
                }
                if (tab.getId() == 0) {
                    if (!isFinishing() && !isDestroyed()) {
                        Util.log("Tab selected ");
                        getSupportFragmentManager().beginTransaction().replace(R.id.landingFragmentContainerView, new ListDatabaseFragment()).commit();
                        Util.log("Tab selected done");
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
        TabLayout.Tab databaseTab = binding.landingTabLayout.newTab();
        databaseTab.setText(R.string.database);
        databaseTab.setIcon(R.drawable.ic_database_fill0_wght300_grad_25_opsz24);
        databaseTab.view.setSelected(true);
        databaseTab.setId(0);
        runOnUiThread(() -> {
            binding.landingTabLayout.addTab(databaseTab, 0);
            Util.log("Added tab");
        });
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
            if (updatePref) {
                updateThemeInPref(theme.getId());
            }
        });
    }

    public void updateThemeInPref(String id) {
        Util.log("Updating theme in pref");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("sTheme", id);
        editor.apply();
        finish();
        startActivity(new Intent(this, this.getClass()));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void showMenu(Context context) {
        Util.log("Show menu via listener");
        new BottomMenuUtil().showLandingMoreOptionsMenu(context);
    }

    @Override
    public void changeThemeIsClickedShowThemes(Context context) {
        Util.log("Change theme option in menu is clicked");
        new BottomMenuUtil().showThemesMenu(context);
    }

    @Override
    public void showCreateNewDb(Context context) {
        Util.log("Showing db create bsd");
        new BottomMenuUtil().showCreateDbBsd(context);
    }


    @Override
    public void showNewPwd(String newPwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        runOnUiThread(() -> {
            new BottomMenuUtil().newPwdBsd(binding.getRoot().getContext(), newPwd, useDigit, useLowerCase, useUpperCase, useSymbol, length);
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

    @Override
    public void showInfo(Context context) {
        new BottomMenuUtil().showInfo(context);
    }

    @Override
    public void createDb(final String dbName, final String pwd) {
        AtomicReference<String> dbNameAr = new AtomicReference<>(dbName);
        AtomicReference<String> dirPath = new AtomicReference<>("");
        AtomicReference<String> subFilesDirPath = new AtomicReference<>("");
        AtomicReference<File> newDbFile = new AtomicReference<>();
        AtomicReference<Boolean> isCodecAvailable = new AtomicReference<>(false);
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.creatingNewDatabase));
            LoadingEventSource.getInstance().showLoading();
        });
        executor.execute(() -> {
            if (dbNameAr.get() == null || dbNameAr.get().length() == 0) {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.enterDatabaseName));
            }
            if (pwd == null || pwd.length() == 0) {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.enterPassword));
            }
            if (!dbName.endsWith("kdbx")) {
                dbNameAr.set(dbName + ".kdbx");
            }
        });
        executor.execute(() -> dirPath.set(new DbAndFileOperations().getDir(this)));
        executor.execute(() -> subFilesDirPath.set(new DbAndFileOperations().getSubDir(this)));
        executor.execute(() -> new DbAndFileOperations().createMainDirectory(dirPath.get()));
        executor.execute(() -> new DbAndFileOperations().createSubFilesDirectory(subFilesDirPath.get()));
        executor.execute(() -> isCodecAvailable.set(Util.checkCodecAvailable()));
        executor.execute(() -> {
            if (isCodecAvailable.get()) {
                newDbFile.set(new DbAndFileOperations().createFile(subFilesDirPath.get(), dbNameAr.get()));
            }
        });
        executor.execute(() -> {
            if (isCodecAvailable.get()) {
                new DbAndFileOperations().writeDbToFile(newDbFile.get(), pwd, getContentResolver());
            }
        });
        executor.execute(() -> {
            if (isCodecAvailable.get()) {
                LoadingEventSource.getInstance().dismissLoading();
            } else {
                LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.devInProgress));
            }
        });
        executor.execute(() -> {
            if (isCodecAvailable.get()) {
                DbEventSource.getInstance().reloadDbFile();
            }
        });
    }

    @Override
    public void reloadDbFile() {
    }

    @Override
    public void askPwdForDb(Context context, String dbName, String fullPath) {
        try {
            new BottomMenuUtil().showAskPwdForDb(context, dbName, fullPath);
        } catch (Throwable e) {
            //ignore
        }
    }

    @Override
    public void failedToOpenDb(String errorMsg) {

    }

    @Override
    public void loadSuccessDb() {
        Intent intent = new Intent(this, ListGroupEntriesActivity.class);
        startActivity(intent);
        finish();
    }
}
