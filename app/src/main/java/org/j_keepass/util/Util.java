package org.j_keepass.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import org.j_keepass.LoadActivity;
import org.j_keepass.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Util {

    static public byte[] object2Bytes(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    static public Object bytes2Object(byte raw[])
            throws Exception, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return o;
    }

    static public AlertDialog getLoading(LayoutInflater layoutInflater, Context context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        View mView = layoutInflater.inflate(R.layout.loading_layout, null);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ProgressBar progressBar = mView.findViewById(R.id.loadingProgressBar);
        progressBar.setProgress(10);
        return alertDialog;
    }

    static public void setLoadingProgress(AlertDialog alertDialog, int progress) {
        ProgressBar progressBar = alertDialog.findViewById(R.id.loadingProgressBar);
        progressBar.setProgress(progress);
    }

    static public void increaseLoadingProgressBy10(AlertDialog alertDialog) {
        ProgressBar progressBar = alertDialog.findViewById(R.id.loadingProgressBar);
        progressBar.setProgress(progressBar.getProgress() + 10);
    }

    static public void dismissLoadingDialog(AlertDialog alertDialog) {
        alertDialog.dismiss();
    }

    static public void showLoadingDialog(AlertDialog alertDialog) {
        alertDialog.show();
    }


    static public AlertDialog getSaving(LayoutInflater layoutInflater, Context context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        View mView = layoutInflater.inflate(R.layout.saving_layout, null);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        ProgressBar progressBar = mView.findViewById(R.id.savingProgressBar);
        progressBar.setProgress(10);
        return alertDialog;
    }

    static public void setSavingProgress(AlertDialog alertDialog, int progress) {
        ProgressBar progressBar = alertDialog.findViewById(R.id.savingProgressBar);
        progressBar.setProgress(progress);
    }

    static public void increaseSavingProgressBy10(AlertDialog alertDialog) {
        ProgressBar progressBar = alertDialog.findViewById(R.id.savingProgressBar);
        progressBar.setProgress(progressBar.getProgress() + 10);
    }

    static public void dismissSavingDialog(AlertDialog alertDialog) {
        alertDialog.dismiss();
    }

    static public void showSavingDialog(AlertDialog alertDialog) {
        alertDialog.show();
    }
}
