package org.j_keepass.util;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.LoadActivity;
import org.j_keepass.R;

public class ThemeSettingDialogUtil {
    private static String sTheme = "wbs";

    static public void show(LayoutInflater layoutInflater, View mView, ClipboardManager clipboard, View anchorView, LoadActivity loadActivity) {
        BottomSheetDialog bsd = new BottomSheetDialog(mView.getContext());
        bsd.setContentView(R.layout.themes_settings_layout);
        bsd.show();
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

    }

    public static void changeToTheme(Activity activity, boolean isFromStart) {
        switch (sTheme) {
            case "br":
                activity.setTheme(R.style.Theme_J_KeePass_br);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "bb":
                activity.setTheme(R.style.Theme_J_KeePass_bb);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "bl":
                activity.setTheme(R.style.Theme_J_KeePass_bl);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "bg":
                activity.setTheme(R.style.Theme_J_KeePass_bg);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "p":
                activity.setTheme(R.style.Theme_J_KeePass_p);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "wr":
                activity.setTheme(R.style.Theme_J_KeePass_wr);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "wl":
                activity.setTheme(R.style.Theme_J_KeePass_wl);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "wb":
                activity.setTheme(R.style.Theme_J_KeePass_wb);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "ws":
                activity.setTheme(R.style.Theme_J_KeePass_ws);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "bs":
                activity.setTheme(R.style.Theme_J_KeePass_bs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "bbs":
                activity.setTheme(R.style.Theme_J_KeePass_bbs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
            case "wbs":
                activity.setTheme(R.style.Theme_J_KeePass_wbs);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

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
                Log.i("JKEEPASS", "sTheme from shared: " + savedSTheme);
                if (savedSTheme != null) {
                    sTheme = savedSTheme;
                }
            } catch (Exception e) {
                Log.i("JKEEPASS", "sTheme error is: ", e);
            }
        }
        Log.i("JKEEPASS", "sTheme is: " + sTheme);
        changeToTheme(activity, true);
    }
}
