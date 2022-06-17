package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import org.j_keepass.R;

public class ConfirmDialogUtil {

    static public Triplet<AlertDialog, MaterialButton, MaterialButton> getConfirmDialog(LayoutInflater layoutInflater, Context
            context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        Triplet<AlertDialog, MaterialButton, MaterialButton> triplet = new Triplet<>();
        try {

            View mView = layoutInflater.inflate(R.layout.confirm_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            MaterialButton yes = mView.findViewById(R.id.confirmYes);
            MaterialButton no = mView.findViewById(R.id.confirmNo);
            no.setOnClickListener(v -> {
                triplet.first.dismiss();
            });

            triplet.second = yes;
            triplet.third = no;
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        triplet.first = alertDialog;
        return triplet;
    }
}
