package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class SearchDialogUtil {

    static public Triplet<AlertDialog, MaterialButton, TextInputEditText> getSearchDialog(LayoutInflater layoutInflater, Context
            context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        Triplet<AlertDialog, MaterialButton, TextInputEditText> triplet = new Triplet<AlertDialog, MaterialButton, TextInputEditText>();
        try {

            View mView = layoutInflater.inflate(R.layout.search_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            ((ScrollView) mView.findViewById(R.id.confirmScrollView)).setAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
            MaterialButton yes = mView.findViewById(R.id.confirmSearch);
            MaterialButton no = mView.findViewById(R.id.searchCancel);
            TextInputEditText searchText = mView.findViewById(R.id.searchText);
            no.setOnClickListener(v -> {
                triplet.first.dismiss();
            });

            triplet.second = yes;
            triplet.third = searchText;
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        triplet.first = alertDialog;
        return triplet;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }
}
