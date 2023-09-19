package org.j_keepass.util;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.R;

public class ToastUtil {

    static public void showToast(LayoutInflater layoutInflater, View
            v, String text, View anchorView) {
        final Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);
        View customSnackView = layoutInflater.inflate(R.layout.toast_layout, null);
        LinearLayout toastLayout = customSnackView.findViewById(R.id.toastLayout);
        TextView toastText = customSnackView.findViewById(R.id.toastText);
        toastText.setText(text);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, (int) v.getContext().getResources().getDimension(R.dimen.kp_toast_height));
        snackbarLayout.addView(customSnackView, 0);
        snackbarLayout.setAnimation(AnimationUtils.loadAnimation(v.getContext(), androidx.transition.R.anim.abc_grow_fade_in_from_bottom));
        snackbarLayout.setBottom(50);
        snackbar.setAnchorView(anchorView);
        snackbar.show();
    }

    static public void showToast(LayoutInflater layoutInflater, View
            v, int id, View anchorView) {
        final Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);
        View customSnackView = layoutInflater.inflate(R.layout.toast_layout, null);
        LinearLayout toastLayout = customSnackView.findViewById(R.id.toastLayout);
        //toastLayout.startAnimation(AnimationUtils.makeInAnimation(layoutInflater.getContext(), true));
        TextView toastText = customSnackView.findViewById(R.id.toastText);
        toastText.setText(id);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        //snackbarLayout.setPadding(0, 0, 0, (int) v.getContext().getResources().getDimension(R.dimen.kp_toast_height));
        snackbarLayout.addView(customSnackView, 0);
        snackbarLayout.setAnimation(AnimationUtils.loadAnimation(v.getContext(), androidx.transition.R.anim.abc_grow_fade_in_from_bottom));
        //snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        snackbar.setAnchorView(anchorView);
        snackbar.show();
    }

    static public void showToast(LayoutInflater layoutInflater, View
            v, KpCustomException exception, View anchorView) {

        if (exception.isUseId()) {
            showToast(layoutInflater, v, exception.getResourceId(), anchorView);
        } else {
            showToast(layoutInflater, v, exception.getMessage(), anchorView);
        }
    }
}
