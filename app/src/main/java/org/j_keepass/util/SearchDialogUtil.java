package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class SearchDialogUtil {

    static public Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> getSearchDialog(LayoutInflater layoutInflater, Context
            context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> triplet = new Triplet<BottomSheetDialog, MaterialButton, TextInputEditText>();
        try {

            bsd.setContentView(R.layout.search_layout);
            MaterialButton yes = bsd.findViewById(R.id.confirmSearch);
            TextInputEditText searchText = bsd.findViewById(R.id.searchText);
            triplet.second = yes;
            triplet.third = searchText;
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
            Util.log("Error expanding search sheet " + e.getMessage());
        }
        triplet.first = bsd;
        return triplet;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
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
