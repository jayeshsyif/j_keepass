package org.j_keepass.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.LoadActivity;
import org.j_keepass.R;

public class ThemeSettingDialogUtil {
    private static String sTheme = "wbs";

    static public void show(LayoutInflater layoutInflater, View mView, ClipboardManager clipboard, View anchorView, LoadActivity loadActivity) {
        BottomSheetDialog bsd = new BottomSheetDialog(mView.getContext());
        bsd.setContentView(R.layout.themes_settings_layout);
        bsd.show();
        try {
            int orientation = mView.getContext().getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding db menu sheet " + e.getMessage());
        }
        LinearLayout wbLl = bsd.findViewById(R.id.wb);
        wbLl.setOnClickListener(view -> {
            sTheme = "wb";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });
        LinearLayout wrLl = bsd.findViewById(R.id.wr);
        wrLl.setOnClickListener(view -> {
            sTheme = "wr";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout wlLl = bsd.findViewById(R.id.wl);
        wlLl.setOnClickListener(view -> {
            sTheme = "wl";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });
        LinearLayout pLl = bsd.findViewById(R.id.p);
        pLl.setOnClickListener(view -> {
            sTheme = "p";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout brLl = bsd.findViewById(R.id.br);
        brLl.setOnClickListener(view -> {
            sTheme = "br";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });
        LinearLayout bbLl = bsd.findViewById(R.id.bb);
        bbLl.setOnClickListener(view -> {
            sTheme = "bb";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });
        LinearLayout blLl = bsd.findViewById(R.id.bl);
        blLl.setOnClickListener(view -> {
            sTheme = "bl";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });
        LinearLayout bgLl = bsd.findViewById(R.id.bg);
        bgLl.setOnClickListener(view -> {
            sTheme = "bg";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout wsLl = bsd.findViewById(R.id.ws);
        wsLl.setOnClickListener(view -> {
            sTheme = "ws";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout bsLl = bsd.findViewById(R.id.bs);
        bsLl.setOnClickListener(view -> {
            sTheme = "bs";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout wbsLl = bsd.findViewById(R.id.wbs);
        wbsLl.setOnClickListener(view -> {
            sTheme = "wbs";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout bbsLl = bsd.findViewById(R.id.bbs);
        bbsLl.setOnClickListener(view -> {
            sTheme = "bbs";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });


        LinearLayout wrsLl = bsd.findViewById(R.id.wrs);
        wrsLl.setOnClickListener(view -> {
            sTheme = "wrs";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

        LinearLayout brsLl = bsd.findViewById(R.id.brs);
        brsLl.setOnClickListener(view -> {
            sTheme = "brs";
            bsd.dismiss();
            changeToTheme(loadActivity, false);
        });

    }

    public static void changeToTheme(Activity activity, boolean isFromStart) {
        boolean isLightTheme = false;
        switch (sTheme) {
            case "br":
                activity.setTheme(R.style.Theme_J_KeePass_br);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "bb":
                activity.setTheme(R.style.Theme_J_KeePass_bb);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "bl":
                activity.setTheme(R.style.Theme_J_KeePass_bl);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "bg":
                activity.setTheme(R.style.Theme_J_KeePass_bg);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "p":
                activity.setTheme(R.style.Theme_J_KeePass_p);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "wr":
                activity.setTheme(R.style.Theme_J_KeePass_wr);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "wl":
                activity.setTheme(R.style.Theme_J_KeePass_wl);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "wb":
                activity.setTheme(R.style.Theme_J_KeePass_wb);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "ws":
                activity.setTheme(R.style.Theme_J_KeePass_ws);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "bs":
                activity.setTheme(R.style.Theme_J_KeePass_bs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "bbs":
                activity.setTheme(R.style.Theme_J_KeePass_bbs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            case "wrs":
                activity.setTheme(R.style.Theme_J_KeePass_wrs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;
            case "brs":
                activity.setTheme(R.style.Theme_J_KeePass_brs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLightTheme = false;
                break;
            default:
            case "wbs":
                activity.setTheme(R.style.Theme_J_KeePass_wbs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLightTheme = true;
                break;

        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Use flags to set light or dark text
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isLightTheme) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                }
            }
        } catch (Exception e) {
            Util.log("Error setting light status " + e.getMessage());
        }
        if (!isFromStart) {
            SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("sTheme", sTheme);
            editor.apply();
            activity.finish();
            activity.startActivity(new Intent(activity, activity.getClass()));
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public static void onActivityCreateSetTheme(Activity activity, boolean loadFromShare) {
        if (loadFromShare) {
            try {
                SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String savedSTheme = sharedPref.getString("sTheme", null);
                Util.log("sTheme from shared: " + savedSTheme);
                if (savedSTheme != null) {
                    sTheme = savedSTheme;
                }
            } catch (Exception e) {
                Util.log("sTheme error is: " + e.getMessage());
            }
        }
        Util.log("sTheme is: " + sTheme);
        changeToTheme(activity, true);
    }

    public static void showFullScreenBottomSheet(FrameLayout bottomSheet) {
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = Resources.getSystem().getDisplayMetrics().heightPixels;
        bottomSheet.setLayoutParams(layoutParams);
    }

    public static void expandBottomSheet(BottomSheetBehavior<FrameLayout> bottomSheetBehavior) {
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}
