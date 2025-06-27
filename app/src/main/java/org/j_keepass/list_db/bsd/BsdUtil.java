package org.j_keepass.list_db.bsd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;
import org.j_keepass.db.operation.DbAndFileOperations;
import org.j_keepass.db.events.DbEventSource;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.adapters.ListThemesAdapter;
import org.j_keepass.list_db.util.themes.Theme;
import org.j_keepass.list_db.util.themes.ThemeUtil;
import org.j_keepass.util.CopyUtil;
import org.j_keepass.util.Utils;
import org.j_keepass.util.confirm_alert.ConfirmNotifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BsdUtil {

    public void showLandingMoreOptionsMenu(Context context, Activity activity) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.list_db_more_option_list);
        TableRow landingMoreOptionChangeTheme = bsd.findViewById(R.id.landingMoreOptionChangeTheme);
        if (landingMoreOptionChangeTheme != null) {
            landingMoreOptionChangeTheme.setOnClickListener(view -> {
                bsd.dismiss();
                showThemesMenu(context);
            });
        }
        TableRow landingMoreOptionCreateDb = bsd.findViewById(R.id.landingMoreOptionCreateDb);
        if (landingMoreOptionCreateDb != null) {
            landingMoreOptionCreateDb.setOnClickListener(view -> {
                bsd.dismiss();
                showCreateDbBsd(view.getContext());
            });
        }
        TableRow landingMoreOptionGenerateNewPassword = bsd.findViewById(R.id.landingMoreOptionGenerateNewPassword);
        if (landingMoreOptionGenerateNewPassword != null) {
            landingMoreOptionGenerateNewPassword.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPwd));
                    LoadingEventSource.getInstance().showLoading();
                });
                executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
            });
        }

        TableRow landingMoreOptionInfo = bsd.findViewById(R.id.landingMoreOptionInfo);
        if (landingMoreOptionInfo != null) {
            landingMoreOptionInfo.setOnClickListener(view -> {
                bsd.dismiss();
                showInfo(view.getContext());
            });
        }

        TableRow landingMoreOptionImportDb = bsd.findViewById(R.id.landingMoreOptionImportDb);
        if (landingMoreOptionImportDb != null) {
            landingMoreOptionImportDb.setOnClickListener(view -> {
                bsd.dismiss();
                PermissionEventSource.getInstance().checkAndGetPermissionReadWriteStorage(view, activity, PermissionEvent.PermissionAction.IMPORT);
            });
        }

        expandBsd(bsd);
        bsd.show();
    }

    private void expandBsd(BottomSheetDialog bsd) {
        FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setSkipCollapsed(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void showThemesMenu(Context context) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.list_themes);
        HashMap<String, Theme> map = ThemeUtil.getThemes();
        ArrayList<Theme> themes = new ArrayList<>();
        for (Map.Entry<String, Theme> entry : map.entrySet()) {
            themes.add(entry.getValue());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(themes, Comparator.comparingInt(Theme::getPos));
        } else {
            Collections.sort(themes, (t1, t2) -> compareInt(t1.getPos(), t2.getPos()));
        }
        ListThemesAdapter adapter = new ListThemesAdapter();
        adapter.setValues(themes);
        adapter.setBsd(bsd);
        RecyclerView recyclerView = bsd.findViewById(R.id.showThemesRecyclerView);
        expandBsd(bsd);
        if (recyclerView != null) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 5);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
        }
        bsd.show();
    }

    private int compareInt(int pos1, int pos2) {
        return Integer.compare(pos1, pos2);
    }

    public void showCreateDbBsd(Context context) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.db_enter_name_and_pwd);
        expandBsd(bsd);
        final MaterialButton saveBtn = bsd.findViewById(R.id.saveDatabase);
        final TextInputEditText dbName = bsd.findViewById(R.id.databaseName);
        final TextInputEditText dbPwd = bsd.findViewById(R.id.databasePassword);
        if (saveBtn != null) {
            saveBtn.setText(R.string.create);
            saveBtn.setOnClickListener(view -> {
                if (dbName != null && dbPwd != null) {
                    Utils.log("Create new db btn is clicked");
                    if (dbName.getText() == null || dbName.getText().toString().length() == 0) {
                        dbName.requestFocus();
                    } else if (dbPwd.getText() == null || dbPwd.getText().toString().length() == 0) {
                        dbPwd.requestFocus();
                    } else {
                        hideKeyboard(view);
                        bsd.dismiss();
                        DbEventSource.getInstance().createDb(dbName.getText().toString(), dbPwd.getText().toString());
                    }
                }
            });
        }
        bsd.show();
    }

    public void showAskPwdForDb(Context context, String dbName, String fullPath) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.db_enter_name_and_pwd);
        expandBsd(bsd);
        final MaterialButton saveBtn = bsd.findViewById(R.id.saveDatabase);
        final TextInputEditText dbNameEt = bsd.findViewById(R.id.databaseName);
        if (dbNameEt != null) {
            dbNameEt.setText(dbName);
        }
        final TextInputLayout databaseNameLayout = bsd.findViewById(R.id.databaseNameLayout);
        if (databaseNameLayout != null) {
            databaseNameLayout.setVisibility(View.GONE);
        }
        TextView dbNameText = bsd.findViewById(R.id.dbNameText);
        if (dbNameText != null) {
            dbNameText.setText(dbName);
        }
        final ImageButton databaseMoreOption = bsd.findViewById(R.id.dbMoreOption);
        if (databaseMoreOption != null) {
            databaseMoreOption.setOnClickListener(view -> {
                bsd.dismiss();
                showMoreSelectedDbOptions(context, dbName, fullPath);
            });
        }
        final TextInputEditText dbPwd = bsd.findViewById(R.id.databasePassword);
        if (saveBtn != null) {
            saveBtn.setText(R.string.open);
            saveBtn.setOnClickListener(view -> {
                Utils.log(" ask pass db btn is clicked");
                if (dbPwd != null) {
                    if (dbPwd.getText() == null || dbPwd.getText().toString().length() == 0) {
                        dbPwd.requestFocus();
                    } else {
                        hideKeyboard(view);
                        bsd.dismiss();
                        String openingStr = view.getContext().getString(R.string.opening);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(openingStr + " " + dbName);
                            LoadingEventSource.getInstance().showLoading();
                        });
                        executor.execute(() -> new DbAndFileOperations().openDb(dbName, dbPwd.getText().toString(), fullPath, view.getContext().getContentResolver()));
                    }
                }
            });
        }
        bsd.show();
    }

    public void showAskPwdForDb(Context context, String dbName, Uri data) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.db_enter_name_and_pwd);
        expandBsd(bsd);
        final MaterialButton saveBtn = bsd.findViewById(R.id.saveDatabase);
        final TextInputEditText dbNameEt = bsd.findViewById(R.id.databaseName);
        if (dbNameEt != null) {
            dbNameEt.setText(dbName);
        }
        final TextInputLayout databaseNameLayout = bsd.findViewById(R.id.databaseNameLayout);
        if (databaseNameLayout != null) {
            databaseNameLayout.setVisibility(View.GONE);
        }
        TextView dbNameText = bsd.findViewById(R.id.dbNameText);
        if (dbNameText != null) {
            dbNameText.setText(dbName);
        }
        final ImageButton databaseMoreOption = bsd.findViewById(R.id.dbMoreOption);
        if (databaseMoreOption != null) {
            databaseMoreOption.setVisibility(View.INVISIBLE);
        }
        final TextInputEditText dbPwd = bsd.findViewById(R.id.databasePassword);
        if (saveBtn != null) {
            saveBtn.setText(R.string.open);
            saveBtn.setOnClickListener(view -> {
                Utils.log(" ask pass db btn is clicked");
                if (dbPwd != null) {
                    if (dbPwd.getText() == null || dbPwd.getText().toString().length() == 0) {
                        dbPwd.requestFocus();
                    } else {
                        hideKeyboard(view);
                        bsd.dismiss();
                        String openingStr = view.getContext().getString(R.string.opening);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(openingStr + " " + dbName);
                            LoadingEventSource.getInstance().showLoading();
                        });
                        executor.execute(() -> new DbAndFileOperations().openDb(dbName, dbPwd.getText().toString(), data, view.getContext().getContentResolver()));
                    }
                }
            });
        }
        bsd.show();
    }

    public void showMoreSelectedDbOptions(Context context, String dbName, String fullPath) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.selected_db_more_option_list);
        final TableRow selectedDbMoreOptionEditDbName = bsd.findViewById(R.id.selectedDbMoreOptionEditDbName);
        if (selectedDbMoreOptionEditDbName != null) {
            selectedDbMoreOptionEditDbName.setOnClickListener(view -> {
                bsd.dismiss();
                showAskEditDbName(context, dbName, fullPath);
            });
        }
        final TableRow selectedDbMoreOptionShare = bsd.findViewById(R.id.selectedDbMoreOptionShare);
        if (selectedDbMoreOptionShare != null) {
            selectedDbMoreOptionShare.setOnClickListener(view -> {
                bsd.dismiss();
                shareDb(context, dbName, fullPath);
            });
        }
        final TableRow selectedDbMoreOptionDeleteDb = bsd.findViewById(R.id.selectedDbMoreOptionDeleteDb);
        if (selectedDbMoreOptionDeleteDb != null) {
            selectedDbMoreOptionDeleteDb.setOnClickListener(view -> {
                bsd.dismiss();
                new org.j_keepass.util.confirm_alert.BsdUtil().show(view.getContext(), new ConfirmNotifier() {
                    @Override
                    public void onYes() {
                        String deletingStr = view.getContext().getString(R.string.deleting);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(deletingStr + " " + dbName);
                            LoadingEventSource.getInstance().showLoading();
                            try {
                                Utils.sleepFor3MSec();
                                File toDelete = new File(Db.getInstance().getAppSubDir() + File.separator + dbName);
                                boolean isDeleted = toDelete.delete();
                                if (isDeleted) {
                                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.EDIT);
                                } else {
                                    String unableToRenameStr = view.getContext().getString(R.string.unableToDelete);
                                    LoadingEventSource.getInstance().updateLoadingText(unableToRenameStr + " " + dbName);
                                }
                            } catch (Throwable t) {
                                String unableToRenameStr = view.getContext().getString(R.string.unableToDelete);
                                LoadingEventSource.getInstance().updateLoadingText(unableToRenameStr + " " + dbName);
                            }
                        });
                    }

                    @Override
                    public void onNo() {
                        // ignore
                    }
                });
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    public void shareDb(Context context, String dbName, String fullPath) {
        File from = new File(Db.getInstance().getAppSubDir() + File.separator + dbName);
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", from);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        sendIntent.setType("*/*");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent shareIntent = Intent.createChooser(sendIntent, "Sharing");
        context.startActivity(shareIntent);
    }

    public void showAskEditDbName(Context context, String dbName, String fullPath) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.db_enter_name_and_pwd);
        expandBsd(bsd);
        final MaterialButton saveBtn = bsd.findViewById(R.id.saveDatabase);
        final TextInputEditText dbNameEt = bsd.findViewById(R.id.databaseName);
        if (dbNameEt != null) {
            dbNameEt.setText(dbName);
        }
        TextView dbNameText = bsd.findViewById(R.id.dbNameText);
        if (dbNameText != null) {
            dbNameText.setText(dbName);
        }
        final ImageButton databaseMoreOption = bsd.findViewById(R.id.dbMoreOption);
        if (databaseMoreOption != null) {
            databaseMoreOption.setOnClickListener(view -> {
                bsd.dismiss();
                showMoreSelectedDbOptions(context, dbName, fullPath);
            });
        }
        final TextInputLayout dbPwd = bsd.findViewById(R.id.databasePasswordLayout);
        if (dbPwd != null) {
            dbPwd.setVisibility(View.GONE);
        }
        if (saveBtn != null) {
            saveBtn.setText(R.string.open);
            saveBtn.setOnClickListener(view -> {
                Utils.log(" db change name is clicked");
                if (dbNameEt != null) {
                    if (dbNameEt.getText() == null || dbNameEt.getText().toString().length() == 0) {
                        dbNameEt.requestFocus();
                    } else {
                        hideKeyboard(view);
                        bsd.dismiss();
                        String newName = dbNameEt.getText().toString();
                        String changingStr = view.getContext().getString(R.string.changing);
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(changingStr + " " + newName);
                            LoadingEventSource.getInstance().showLoading();
                            try {
                                File to = new File(Db.getInstance().getAppSubDir() + File.separator + newName);
                                File from = new File(Db.getInstance().getAppSubDir() + File.separator + dbName);
                                Utils.sleepFor3MSec();
                                boolean isRenamed = from.renameTo(to);
                                if (isRenamed) {
                                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.EDIT);
                                } else {
                                    String unableToRenameStr = view.getContext().getString(R.string.unableToRename);
                                    LoadingEventSource.getInstance().updateLoadingText(unableToRenameStr + " " + newName);
                                }
                            } catch (Throwable t) {
                                String unableToRenameStr = view.getContext().getString(R.string.unableToRename);
                                LoadingEventSource.getInstance().updateLoadingText(unableToRenameStr + " " + newName);
                            }
                        });
                    }
                }
            });
        }
        bsd.show();
    }

    public void newPwdBsd(Context context, String pwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.new_password);
        TextView newPasswordTextView = bsd.findViewById(R.id.newPasswordTextView);
        if (newPasswordTextView != null) {
            newPasswordTextView.setText(pwd);
        }
        MaterialCheckBox useDigitMcb = bsd.findViewById(R.id.useDigit);
        MaterialCheckBox useLowerCaseMcb = bsd.findViewById(R.id.useLowerCase);
        MaterialCheckBox useUpperCaseMcb = bsd.findViewById(R.id.useUpperCase);
        MaterialCheckBox useSymbolMcb = bsd.findViewById(R.id.useSymbol);
        MaterialButton reGenerateNewPassword = bsd.findViewById(R.id.reGenerateNewPassword);
        ImageButton newPasswordCopy = bsd.findViewById(R.id.newPasswordCopy);
        if (useDigitMcb != null) {
            useDigitMcb.setChecked(useDigit);
        }
        if (useLowerCaseMcb != null) {
            useLowerCaseMcb.setChecked(useLowerCase);
        }
        if (useUpperCaseMcb != null) {
            useUpperCaseMcb.setChecked(useUpperCase);
        }
        if (useSymbolMcb != null) {
            useSymbolMcb.setChecked(useSymbol);
        }
        Slider slider = bsd.findViewById(R.id.newPasswordSlider);
        if (slider != null) {
            slider.setValue((float) length);
        }
        if (newPasswordCopy != null) {
            newPasswordCopy.setOnClickListener(view -> {
                bsd.dismiss();
                CopyUtil.copyToClipboard(view.getContext(), pwd, pwd);
            });
        }
        if (reGenerateNewPassword != null) {
            reGenerateNewPassword.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPwd));
                    LoadingEventSource.getInstance().showLoading();
                });
                if (useDigitMcb != null && useLowerCaseMcb != null && useUpperCaseMcb != null && useSymbolMcb != null && slider != null) {
                    executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd(useDigitMcb.isChecked(), useLowerCaseMcb.isChecked(), useUpperCaseMcb.isChecked(), useSymbolMcb.isChecked(), Float.valueOf(slider.getValue()).intValue()));
                }
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    public void showInfo(Context context) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.info);
        ImageButton linkedInLink = bsd.findViewById(R.id.linkedInLink);
        if (linkedInLink != null) {
            linkedInLink.setOnClickListener(v -> {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.linkedin.com/in/jayesh-ganatra-76051056"));
                context.startActivity(intent);
            });
        }
        ImageButton cvLink = bsd.findViewById(R.id.cvLink);
        if (cvLink != null) {
            cvLink.setOnClickListener(v -> {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://flowcv.me/jayesh-ganatra"));
                context.startActivity(intent);
            });
        }

        ImageButton playStoreLink = bsd.findViewById(R.id.playStoreLink);
        if (playStoreLink != null) {
            playStoreLink.setOnClickListener(v -> {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/dev?id=7560962222107226464"));
                context.startActivity(intent);
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
