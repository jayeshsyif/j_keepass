package org.j_keepass.util;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.R;

public class ToastUtil {

    static public void showToast(LayoutInflater layoutInflater, View
            v, String text) {
        final Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);
        View customSnackView = layoutInflater.inflate(R.layout.toast_layout, null);
        LinearLayout toastLayout = customSnackView.findViewById(R.id.toastLayout);
        TextView toastText = customSnackView.findViewById(R.id.toastText);
        toastText.setText(text);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(customSnackView, 0);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.show();
    }

    static public void showToast(LayoutInflater layoutInflater, View
            v, int id) {
        final Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);
        View customSnackView = layoutInflater.inflate(R.layout.toast_layout, null);
        LinearLayout toastLayout = customSnackView.findViewById(R.id.toastLayout);
        //toastLayout.startAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
        TextView toastText = customSnackView.findViewById(R.id.toastText);
        toastText.setText(id);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(customSnackView, 0);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.show();
    }

    static public void showToast(LayoutInflater layoutInflater, View
            v, KpCustomException exception) {

        if (exception.isUseId()) {
            showToast(layoutInflater, v, exception.getResourceId());
        } else {
            showToast(layoutInflater, v, exception.getMessage());
        }
    }
}
