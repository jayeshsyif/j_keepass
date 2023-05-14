package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;

public class DatabaseCreateDialogUtil {

    static public Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> getConfirmDialog(LayoutInflater layoutInflater, Context
            context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> penta = new Penta<>();
        try {

            View mView = layoutInflater.inflate(R.layout.database_name_password_dialog_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            //((ScrollView) mView.findViewById(R.id.confirmScrollView)).setAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
            MaterialButton save = mView.findViewById(R.id.saveDatabase);
            FloatingActionButton cancel = mView.findViewById(R.id.cancelDatabase);
            cancel.setOnClickListener(v -> {
                penta.first.dismiss();
            });

            penta.second = save;
            penta.third = cancel;
            penta.fourth = mView.findViewById(R.id.databaseName);
            penta.fifth = mView.findViewById(R.id.databasePassword);
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        penta.first = alertDialog;
        return penta;
    }

    static public void showDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }

    static public Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> getConfirmDialogEditName(LayoutInflater layoutInflater, Context
            context, String value) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> penta = new Penta<>();
        try {

            View mView = layoutInflater.inflate(R.layout.database_name_password_dialog_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            //((ScrollView) mView.findViewById(R.id.confirmScrollView)).setAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
            LinearLayout databasePasswordLayout = mView.findViewById(R.id.databasePasswordLayout);
            databasePasswordLayout.setVisibility(View.GONE);
            MaterialButton save = mView.findViewById(R.id.saveDatabase);
            FloatingActionButton cancel = mView.findViewById(R.id.cancelDatabase);
            cancel.setOnClickListener(v -> {
                penta.first.dismiss();
            });

            penta.second = save;
            penta.third = cancel;
            penta.fourth = mView.findViewById(R.id.databaseName);
            penta.fourth.setText(value);
            penta.fifth = mView.findViewById(R.id.databasePassword);
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        penta.first = alertDialog;
        return penta;
    }

    static public Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> getConfirmDialogChangePassword(LayoutInflater layoutInflater, Context
            context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        Penta<AlertDialog, MaterialButton, FloatingActionButton, TextInputEditText, TextInputEditText> penta = new Penta();
        try {

            View mView = layoutInflater.inflate(R.layout.database_change_password_dialog_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            //((ScrollView) mView.findViewById(R.id.confirmScrollView)).setAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
            MaterialButton save = mView.findViewById(R.id.saveDatabase);
            FloatingActionButton cancel = mView.findViewById(R.id.cancelDatabase);
            cancel.setOnClickListener(v -> {
                penta.first.dismiss();
            });

            penta.second = save;
            penta.third = cancel;
            penta.fourth = mView.findViewById(R.id.databaseOldPassword);
            penta.fifth = mView.findViewById(R.id.databaseNewPassword);
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        penta.first = alertDialog;
        return penta;
    }
}
