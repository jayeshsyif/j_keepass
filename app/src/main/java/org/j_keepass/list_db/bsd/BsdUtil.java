package org.j_keepass.list_db.bsd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;
import org.j_keepass.list_db.adapters.ListThemesAdapter;
import org.j_keepass.db.event.DbAndFileOperations;
import org.j_keepass.db.event.DbEventSource;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionEventSource;
import org.j_keepass.util.CopyUtil;
import org.j_keepass.util.Utils;
import org.j_keepass.list_db.util.themes.Theme;
import org.j_keepass.list_db.util.themes.ThemeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BsdUtil {

    public void showLandingMoreOptionsMenu(Context context, Activity activity) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.list_db_more_option_list);
        LinearLayout landingMoreOptionChangeTheme = bsd.findViewById(R.id.landingMoreOptionChangeTheme);
        if (landingMoreOptionChangeTheme != null) {
            landingMoreOptionChangeTheme.setOnClickListener(view -> {
                bsd.dismiss();
                showThemesMenu(context);
            });
        }
        LinearLayout landingMoreOptionCreateDb = bsd.findViewById(R.id.landingMoreOptionCreateDb);
        if (landingMoreOptionCreateDb != null) {
            landingMoreOptionCreateDb.setOnClickListener(view -> {
                bsd.dismiss();
                showCreateDbBsd(view.getContext());
            });
        }
        LinearLayout landingMoreOptionGenerateNewPassword = bsd.findViewById(R.id.landingMoreOptionGenerateNewPassword);
        if (landingMoreOptionGenerateNewPassword != null) {
            landingMoreOptionGenerateNewPassword.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPassword));
                    LoadingEventSource.getInstance().showLoading();
                });
                executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
            });
        }

        LinearLayout landingMoreOptionInfo = bsd.findViewById(R.id.landingMoreOptionInfo);
        if (landingMoreOptionInfo != null) {
            landingMoreOptionInfo.setOnClickListener(view -> {
                bsd.dismiss();
                showInfo(view.getContext());
            });
        }

        LinearLayout landingMoreOptionImportDb = bsd.findViewById(R.id.landingMoreOptionImportDb);
        if (landingMoreOptionImportDb != null) {
            landingMoreOptionImportDb.setOnClickListener(view -> {
                bsd.dismiss();
                PermissionEventSource.getInstance().checkAndGetPermissionReadWriteStorage(view, activity, PermissionEvent.Action.IMPORT);
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
        Collections.sort(themes, (t1, t2) -> Integer.compare(t1.getPos(), t2.getPos()));
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
                Utils.log("Create new db btn is clicked");
                if (dbName != null && dbName.getText() == null) {
                    dbName.requestFocus();
                } else if (dbPwd != null && dbPwd.getText() == null) {
                    dbPwd.requestFocus();
                } else {
                    hideKeyboard(view);
                    bsd.dismiss();
                    DbEventSource.getInstance().createDb(dbName.getText().toString(), dbPwd.getText().toString());
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
            dbNameEt.setVisibility(View.GONE);
        }
        TextView dbNameText = bsd.findViewById(R.id.dbNameText);
        if (dbNameText != null) {
            dbNameText.setText(dbName);
        }
        final TextInputEditText dbPwd = bsd.findViewById(R.id.databasePassword);
        if (saveBtn != null) {
            saveBtn.setText(R.string.open);
            saveBtn.setOnClickListener(view -> {
                Utils.log(" ask pass db btn is clicked");
                if (dbPwd != null && dbPwd.getText() == null) {
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
            });
        }
        bsd.show();
    }

    public void newPwdBsd(Context context, String pwd, boolean useDigit, boolean useLowerCase, boolean useUpperCase, boolean useSymbol, int length) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.new_password);
        ((TextView) bsd.findViewById(R.id.newPasswordTextView)).setText(pwd);
        MaterialCheckBox useDigitMcb = bsd.findViewById(R.id.useDigit);
        MaterialCheckBox useLowerCaseMcb = bsd.findViewById(R.id.useLowerCase);
        MaterialCheckBox useUpperCaseMcb = bsd.findViewById(R.id.useUpperCase);
        MaterialCheckBox useSymbolMcb = bsd.findViewById(R.id.useSymbol);
        MaterialButton reGenerateNewPassword = bsd.findViewById(R.id.reGenerateNewPassword);
        ImageButton newPasswordCopy = bsd.findViewById(R.id.newPasswordCopy);
        useDigitMcb.setChecked(useDigit);
        useLowerCaseMcb.setChecked(useLowerCase);
        useUpperCaseMcb.setChecked(useUpperCase);
        useSymbolMcb.setChecked(useSymbol);
        Slider slider = bsd.findViewById(R.id.newPasswordSlider);
        slider.setValue((float) length);
        newPasswordCopy.setOnClickListener(view -> {
            bsd.dismiss();
            CopyUtil.copyToClipboard(view.getContext(),pwd,pwd);
        });
        reGenerateNewPassword.setOnClickListener(view -> {
            bsd.dismiss();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPassword));
                LoadingEventSource.getInstance().showLoading();
            });
            executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd(useDigitMcb.isChecked(), useLowerCaseMcb.isChecked(), useUpperCaseMcb.isChecked(), useSymbolMcb.isChecked(), Float.valueOf(slider.getValue()).intValue()));
        });
        expandBsd(bsd);
        bsd.show();
    }

    public void showInfo(Context context) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.info);
        ImageButton link = bsd.findViewById(R.id.llink);
        link.setOnClickListener(v -> {
            bsd.dismiss();
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.linkedin.com/in/jayesh-ganatra-76051056"));
            context.startActivity(intent);
        });

        ImageButton elink = bsd.findViewById(R.id.elink);
        elink.setOnClickListener(v -> {
            bsd.dismiss();
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://flowcv.me/jayesh-ganatra"));
            context.startActivity(intent);
        });

        ImageButton glink = bsd.findViewById(R.id.glink);
        glink.setOnClickListener(v -> {
            bsd.dismiss();
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/dev?id=7560962222107226464"));
            context.startActivity(intent);
        });
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
