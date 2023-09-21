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
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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

    static public Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> getConfirmDialogChangePassword(String name, Context
            context) {
        BottomSheetDialog bsd  = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.database_change_password_dialog_layout);
        Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> quadruple = new Quadruple();
        try {

            MaterialButton save = bsd.findViewById(R.id.saveDatabase);
            quadruple.second = save;
            quadruple.third = bsd.findViewById(R.id.databaseOldPassword);
            quadruple.fourth = bsd.findViewById(R.id.databaseNewPassword);
            ((TextView)bsd.findViewById(R.id.nameMenuText)).setText(name);
        } catch (Exception e) {
        }
        quadruple.first = bsd;
        return quadruple;
    }
}
