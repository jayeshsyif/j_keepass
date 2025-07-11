package org.j_keepass.fields.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.google.android.material.tabs.TabLayout;

import org.j_keepass.R;
import org.j_keepass.databinding.FieldActivityLayoutBinding;
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
import org.j_keepass.fields.fragments.FieldFragment;
import org.j_keepass.list_db.bsd.BsdUtil;
import org.j_keepass.list_db.util.themes.SetTheme;
import org.j_keepass.list_db.util.themes.Theme;
import org.j_keepass.list_group_and_entry.activities.ListGroupAndEntriesActivity;
import org.j_keepass.util.SleepFor1Ms;
import org.j_keepass.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FieldActivity extends AppCompatActivity implements ThemeEvent, GenerateNewPwdEvent, PermissionResultEvent, ChangeActivityEvent {
    private FieldActivityLayoutBinding binding;
    ArrayList<ExecutorService> executorServices = new ArrayList<>();
    private boolean isEdit = false;
    private boolean isNew = false;
    private ActivityResultLauncher<Intent> importLauncher = null;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new SetTheme(this, false).run();
        binding = FieldActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        importLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> onActivityResult(result.getResultCode(), result.getData()));
        configureBackPressed();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        isNew = intent.getBooleanExtra("isNew", false);
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
        PermissionResultEventSource.getInstance().addListener(this);
        ChangeActivityEventSource.getInstance().addListener(this);
    }

    private void unregister() {
        GenerateNewPasswordEventSource.getInstance().removeListener(this);
        PermissionResultEventSource.getInstance().removeListener(this);
        ChangeActivityEventSource.getInstance().removeListener(this);
    }

    private void configureBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                binding.entryBackBtn.performClick();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void configureClicks() {
        binding.entryMoreOption.setOnClickListener(view -> new org.j_keepass.fields.bsd.BsdUtil().showMoreOptions(view.getContext(), Db.getInstance().getEntryTitle(Db.getInstance().getCurrentEntryId()), (isEdit || isNew), this));
        binding.entryBackBtn.setOnClickListener(view -> {
            ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
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
                updateCacheEntry(Db.getInstance().getCurrentEntryId());
                startActivity(intent);
                finish();
            }));
        });
        binding.entryGenerateNewPasswordBtn.setOnClickListener(view -> {
            ExecutorService executor = getExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.generatingNewPwd));
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
                        bundle.putBoolean("isNew", isNew);
                        if (isEdit || isNew) {
                            updateCacheEntry(Db.getInstance().getCurrentEntryId());
                        }
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
                        bundle.putBoolean("isNew", isNew);
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
                        bundle.putBoolean("isNew", isNew);
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
        Utils.log("Max id after Adding tabs " + id);
    }

    @Override
    public void applyTheme(Theme theme, boolean updatePref) {
        runOnUiThread(() -> {
            Utils.log("Applying theme " + theme.asString());
            setTheme(theme.getResId());
            AppCompatDelegate.setDefaultNightMode(theme.getMode());
            try {
                setSystemBarLight(this);
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

    @Override
    public void permissionDenied(PermissionEvent.PermissionAction permissionAction) {
        Utils.log("Upload Permission Not Granted for action " + permissionAction.name());
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
            chooseFile.setType("*/*");
            chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            chooseFile = Intent.createChooser(chooseFile, "Choose a file");
            importLauncher.launch(chooseFile);
        } else if (permissionAction != null && permissionAction.name().equals(PermissionEvent.PermissionAction.ALARM.name())) {
            Utils.log("Landing Alarm Permission Granted, setting notification");
            new org.j_keepass.notification.Util().startAlarmBroadcastReceiver(binding.getRoot().getContext());
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        if (resultCode == -1) {
            ExecutorService executor = getExecutor();
            executor.execute(() -> addBinaryProp(data.getData()));
        }
    }

    private void addBinaryProp(Uri dataUri) {
        Utils.log("File received for add binary prop");
        ExecutorService executor = getExecutor();
        executor.execute(() -> {
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.attaching));
            LoadingEventSource.getInstance().showLoading();
            Db.getInstance().addBinaryProp(Db.getInstance().getCurrentEntryId(), dataUri, getContentResolver(), this);
            LoadingEventSource.getInstance().updateLoadingText(binding.getRoot().getContext().getString(R.string.done));
            ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.ENTRY_PROP_UPDATE);
        });
    }

    @Override
    public void changeActivity(ChangeActivityAction changeActivityAction) {
        if (changeActivityAction != null && changeActivityAction.name().equals(ChangeActivityAction.ENTRY_SELECTED_FOR_EDIT.name())) {
            runOnUiThread(() -> binding.entryEditBtn.performClick());
        } else if (changeActivityAction != null && (changeActivityAction.name().equals(ChangeActivityAction.ENTRY_DELETED.name()) || changeActivityAction.name().equals(ChangeActivityAction.ENTRY_COPIED_MOVED.name()))) {
            runOnUiThread(() -> binding.entryBackBtn.performClick());
        } else if (changeActivityAction != null && changeActivityAction.name().equals(ChangeActivityAction.ENTRY_SHARE_AS_TEXT.name())) {
            String entryAsText = Db.getInstance().entryToText(Db.getInstance().getCurrentEntryId());
            if (entryAsText != null) {
                LoadingEventSource.getInstance().dismissLoading();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, entryAsText);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, "Sharing as Text");
                startActivity(shareIntent);
            } else {
                LoadingEventSource.getInstance().updateLoadingText("Unable to share.");
            }
            /*Bitmap bitmap = getBitmapFromView(binding.entryFragmentContainerView);
            try {
                Uri uri = saveBitmapToFile(this, bitmap);
                shareImage(uri);
            } catch (Exception e) {
            }*/

        }
    }

    public Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public Uri saveBitmapToFile(Context context, Bitmap bitmap) throws Exception {
        File imagesFolder = new File(context.getCacheDir(), "images");
        imagesFolder.mkdirs();
        File file = new File(imagesFolder, "shared_image.png");

        FileOutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.flush();
        stream.close();

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public void shareImage(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share Image"));
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
