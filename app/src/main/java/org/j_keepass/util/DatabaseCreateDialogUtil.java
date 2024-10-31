package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.j_keepass.R;

public class DatabaseCreateDialogUtil {

    static public Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> getConfirmDialog(LayoutInflater layoutInflater, Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_name_password_dialog_layout);
        Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> quadruple = new Quadruple<>();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);

            quadruple.second = save;
            quadruple.third = bsd.findViewById(R.id.databaseName);
            quadruple.fourth = bsd.findViewById(R.id.databasePassword);
        } catch (Exception e) {
        }
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding get confirm sheet " + e.getMessage());
        }
        quadruple.first = bsd;
        return quadruple;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }

    static public Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> getConfirmDialogEditName(Context context, String value) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_name_password_dialog_layout);
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> triplet = new Triplet<>();
        try {

            LinearLayout databasePasswordLayout = bsd.findViewById(R.id.databasePasswordLayout);
            databasePasswordLayout.setVisibility(View.GONE);
            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            triplet.second = save;
            triplet.third = bsd.findViewById(R.id.databaseName);
            triplet.third.setText(value);
        } catch (Exception e) {
        }
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding get cofirm dialog edit name sheet " + e.getMessage());
        }
        triplet.first = bsd;
        return triplet;
    }

    static public Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> getConfirmDialogChangePassword(String name, Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_change_password_dialog_layout);
        Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> quadruple = new Quadruple();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            quadruple.second = save;
            quadruple.third = bsd.findViewById(R.id.databaseOldPassword);
            quadruple.fourth = bsd.findViewById(R.id.databaseNewPassword);
            ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(name);
        } catch (Exception e) {
        }
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding change password sheet " + e.getMessage());
        }
        quadruple.first = bsd;
        return quadruple;
    }

    static public Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> getOpenDialog(String name, Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_change_password_dialog_layout);
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> triplet = new Triplet();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            save.setText(R.string.open);
            triplet.second = save;
            bsd.findViewById(R.id.databaseOldPasswordLayout).setVisibility(View.GONE);
            triplet.third = bsd.findViewById(R.id.databaseNewPassword);
            ((TextInputLayout)bsd.findViewById(R.id.databaseNewPasswordLayout)).setHint(R.string.enterPassword);
            ((TextView) bsd.findViewById(R.id.nameMenuText)).setText(name);
        } catch (Exception e) {
        }
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {

                FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                expandBottomSheet(bottomSheetBehavior);
            }
        } catch (Exception e) {
            Util.log("Error expanding open dialog sheet " + e.getMessage());
        }
        triplet.first = bsd;
        return triplet;
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
