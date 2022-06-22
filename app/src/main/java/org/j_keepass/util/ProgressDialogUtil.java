package org.j_keepass.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import org.j_keepass.R;


public class ProgressDialogUtil {


    enum TYPE_OF_PROGREES {LOADING, SAVING, PROCESSING, NONE}

    ;


    static public AlertDialog getLoading(LayoutInflater layoutInflater, Context context) {
        return getProgressDialog(layoutInflater, context, TYPE_OF_PROGREES.LOADING);
    }

    static public void setLoadingProgress(AlertDialog alertDialog, int progress) {
        setProgress(alertDialog, progress, TYPE_OF_PROGREES.LOADING);
    }


    static public void dismissLoadingDialog(AlertDialog alertDialog) {
        alertDialog.dismiss();
    }

    static public void showLoadingDialog(AlertDialog alertDialog) {
        alertDialog.show();
    }


    static public AlertDialog getSaving(LayoutInflater layoutInflater, Context context) {
        return getProgressDialog(layoutInflater, context, TYPE_OF_PROGREES.SAVING);
    }

    static public void setSavingProgress(AlertDialog alertDialog, int progress) {
        setProgress(alertDialog, progress, TYPE_OF_PROGREES.SAVING);
    }

    static public void dismissSavingDialog(AlertDialog alertDialog) {
        try {
            alertDialog.dismiss();
        } catch (Exception e) {
            // do nothing
        }
    }

    static public void showSavingDialog(AlertDialog alertDialog) {
        try {
            alertDialog.show();
        } catch (Exception e) {
            // do nothing
        }
    }


    static public AlertDialog getProgressDialog(LayoutInflater layoutInflater, Context
            context, TYPE_OF_PROGREES typeOfProgrees) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        AlertDialog alertDialog = null;
        try {

            View mView = layoutInflater.inflate(R.layout.progress_layout, null);
            alert.setView(mView);
            alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.getWindow().getAttributes().windowAnimations = BaseTransientBottomBar.ANIMATION_MODE_SLIDE;
            alertDialog.getWindow().setGravity(Gravity.CENTER);
            ProgressBar progressBar = mView.findViewById(R.id.progressBar);
            progressBar.setProgress(0);
            TextView progressBarTextView = mView.findViewById(R.id.progressBarText);
            String progressWithPercentage = context.getString(R.string.loading);
            if (typeOfProgrees == TYPE_OF_PROGREES.LOADING) {
                progressWithPercentage = context.getString(R.string.loadingWithPercentage);
                progressWithPercentage = progressWithPercentage.replace("$per$", "" + 0);
                progressBarTextView.setText(progressWithPercentage);
            } else if (typeOfProgrees == TYPE_OF_PROGREES.SAVING) {
                progressWithPercentage = context.getString(R.string.savingWithPercentage);
                progressWithPercentage = progressWithPercentage.replace("$per$", "" + 0);
                progressBarTextView.setText(progressWithPercentage);
            }
        } catch (Exception e) {
            alertDialog = alert.create();
        }
        return alertDialog;
    }

    static public void setProgress(AlertDialog alertDialog, int progress, TYPE_OF_PROGREES
            typeOfProgrees) {
        try {
            ProgressBar progressBar = alertDialog.findViewById(R.id.progressBar);
            progressBar.setProgress(progress);
            String progressWithPercentage = alertDialog.getContext().getString(R.string.loading);
            TextView progressBarTextView = alertDialog.findViewById(R.id.progressBarText);
            if (typeOfProgrees == TYPE_OF_PROGREES.LOADING) {
                progressWithPercentage = alertDialog.getContext().getString(R.string.loadingWithPercentage);
                progressWithPercentage = progressWithPercentage.replace("$per$", "" + progress);
            } else if (typeOfProgrees == TYPE_OF_PROGREES.SAVING) {
                progressWithPercentage = alertDialog.getContext().getString(R.string.savingWithPercentage);
                progressWithPercentage = progressWithPercentage.replace("$per$", "" + progress);
            }
            progressBarTextView.setText(progressWithPercentage);
        } catch (Exception e) {
            //do nothing
        }
    }

}
