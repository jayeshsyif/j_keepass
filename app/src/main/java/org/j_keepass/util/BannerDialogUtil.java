package org.j_keepass.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.j_keepass.R;

public class BannerDialogUtil {


    @SuppressLint("ResourceType")
    static public Dialog getBanner(Context
            context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        Dialog dialog = null;
        try {

            dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.banner_layout);
            TextView tx;
            tx = dialog.findViewById(R.id.bannerText);
            //tx.startAnimation(AnimationUtils.loadAnimation(context, R.animator.blink));

        } catch (Exception e) {
            Log.e("JKEEPASS", "banner create error ",e);
        }
        return dialog;
    }
}
