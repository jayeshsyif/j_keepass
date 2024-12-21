package org.j_keepass.fields.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.FieldActivityLayoutBinding;
import org.j_keepass.fields.fragments.FieldFragment;
import org.j_keepass.list_group_and_entry.activities.ListGroupAndEntriesActivity;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.newpwd.GenerateNewPwdEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.events.themes.ThemeEvent;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Utils;
import org.j_keepass.list_db.bsd.BsdUtil;
import org.j_keepass.db.event.operations.Db;
import org.j_keepass.list_db.util.themes.SetTheme;
import org.j_keepass.list_db.util.themes.Theme;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FieldActivity extends AppCompatActivity implements ThemeEvent, GenerateNewPwdEvent {
    private FieldActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();

    private boolean isEdit = false;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = FieldActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        if (isEdit) {
            binding.entryEditBtn.setVisibility(View.GONE);
            binding.entrySaveBtn.setVisibility(View.VISIBLE);
        } else {
            binding.entryEditBtn.setVisibility(View.VISIBLE);
            binding.entrySaveBtn.setVisibility(View.GONE);
        }
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
        GenerateNewPasswordEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        GenerateNewPasswordEventSource.getInstance().removeListener(this);
    }

    private void configureClicks() {
        binding.entryHomeBtn.setOnClickListener(view -> {
            Db.getInstance().setCurrentGroupId(Db.getInstance().getRootGroupId());
            ReloadEventSource.getInstance().reload();
            Intent intent = new Intent(this, ListGroupAndEntriesActivity.class);
            startActivity(intent);
            finish();
        });
        binding.entryEditBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.opening));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> runOnUiThread(() -> {
                Intent intent = new Intent(this, FieldActivity.class);
                intent.putExtra("isEdit", true);
                startActivity(intent);
                finish();
            }));
        });
        binding.entryGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.generatingNewPassword));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
        });
        binding.entrySaveBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.saving));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> updateEntry(Db.getInstance().getCurrentEntryId()));
            executor.execute(() -> runOnUiThread(() -> {
                Intent intent = new Intent(this, ListGroupAndEntriesActivity.class);
                intent.putExtra("isEdit", true);
                startActivity(intent);
                finish();
            }));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.log("Entry act destroy");
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

    private void configureTabLayout() {
        Utils.log("Configure entry Tab Layout");
        binding.entryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Utils.log("Tab selected " + tab.getId());
                tab.view.setBackgroundResource(R.drawable.tab_selected_indicator);
                if (tab.getId() == 0) {
                    tab.setIcon(R.drawable.ic_key_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        FieldFragment fieldFragment = new FieldFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "base");
                        bundle.putBoolean("isEdit", isEdit);
                        fieldFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.entryFragmentContainerView, fieldFragment).commit();
                    }
                } else if (tab.getId() == 1) {
                    tab.setIcon(R.drawable.ic_list_fill1_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        FieldFragment fieldFragment = new FieldFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "additional");
                        bundle.putBoolean("isEdit", isEdit);
                        fieldFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.entryFragmentContainerView, fieldFragment).commit();
                    }
                } else if (tab.getId() == 2) {
                    tab.setIcon(R.drawable.ic_attachment_fill0_wght300_grad_25_opsz24);
                    if (!isFinishing() && !isDestroyed()) {
                        FieldFragment fieldFragment = new FieldFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("show", "attachment");
                        bundle.putBoolean("isEdit", isEdit);
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
                } else if (tab.getId() == 2) {
                    tab.setIcon(R.drawable.ic_attachment_fill0_wght300_grad_25_opsz24);
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
            TabLayout.Tab databaseTab = binding.entryTabLayout.newTab();
            databaseTab.setText(R.string.entry);
            databaseTab.setIcon(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.entryTabLayout.addTab(databaseTab, databaseTab.getId());
                Utils.log("Added tab");
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
                Utils.log("Added tab");
            });
        }
        {
            TabLayout.Tab databaseTab = binding.entryTabLayout.newTab();
            databaseTab.setText(R.string.attachments);
            databaseTab.setIcon(R.drawable.ic_attachment_fill0_wght300_grad_25_opsz24);
            databaseTab.view.setSelected(true);
            databaseTab.setId(id);
            id++;
            runOnUiThread(() -> {
                binding.entryTabLayout.addTab(databaseTab, databaseTab.getId());
                Utils.log("Added tab");
            });
        }
    }

    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Utils.log("Applying theme " + theme.asString());
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
                Utils.log("Error setting light status " + e.getMessage());
            }
        });
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

    public void updateCacheEntry(UUID eId) {
        Utils.log("Updating Cached entry");
        Db.getInstance().updateCacheEntry(eId);
    }

    public void updateEntry(UUID eId) {
        Db.getInstance().updateDb(getContentResolver());
        Db.getInstance().updateEntry(eId);
    }
}
