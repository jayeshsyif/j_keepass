package org.j_keepass.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.R;

import java.util.ArrayList;

public class BottomMenuUtil {

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getDbMenuOptions(String dbName, Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        ArrayList<LinearLayout> options = new ArrayList<>();
        options.add(bsd.findViewById(R.id.editMenuLinearLayout));
        options.add(bsd.findViewById(R.id.changePwdMenuLinearLayout));
        options.add(bsd.findViewById(R.id.deleteMenuLinearLayout));
        bsd.findViewById(R.id.addEntryMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addGroupMenuLinearLayout).setVisibility(View.GONE);
        ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(dbName);
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding db menu sheet " + e.getMessage());
        }
        res.first = bsd;
        res.second = options;
        return res;
    }

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getEntryAndGroupMenuOptions(String name, Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        ArrayList<LinearLayout> options = new ArrayList<>();
        bsd.findViewById(R.id.changePwdMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addEntryMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.addGroupMenuLinearLayout).setVisibility(View.GONE);
        options.add(bsd.findViewById(R.id.editMenuLinearLayout));
        options.add(bsd.findViewById(R.id.deleteMenuLinearLayout));
        bsd.findViewById(R.id.moveMenuLinearLayout).setVisibility(View.VISIBLE);
        options.add(bsd.findViewById(R.id.moveMenuLinearLayout));
        bsd.findViewById(R.id.copyMenuLinearLayout).setVisibility(View.VISIBLE);
        options.add(bsd.findViewById(R.id.copyMenuLinearLayout));
        ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(name);
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding entry and group sheet " + e.getMessage());
        }
        res.first = bsd;
        res.second = options;
        return res;
    }

    public static Pair<BottomSheetDialog, ArrayList<LinearLayout>> getAddNewEntryAndGroupMenuOptions(Context context) {
        Pair<BottomSheetDialog, ArrayList<LinearLayout>> res = new Pair<>();
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.bottom_diaglog_menu);
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding new entry and group sheet " + e.getMessage());
        }
        res.first = bsd;
        ArrayList<LinearLayout> options = new ArrayList<>();
        options.add(bsd.findViewById(R.id.addGroupMenuLinearLayout));
        options.add(bsd.findViewById(R.id.addEntryMenuLinearLayout));
        bsd.findViewById(R.id.editMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.changePwdMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.deleteMenuLinearLayout).setVisibility(View.GONE);
        bsd.findViewById(R.id.nameMenuLinearLayout).setVisibility(View.GONE);
        res.second = options;
        return res;
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
