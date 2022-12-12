package org.j_keepass.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.j_keepass.R;

public class InfoDialogUtil {

    public Dialog getInfoDialog(Context context, LayoutInflater inflater) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.info_layout);
        ImageButton link = dialog.findViewById(R.id.llink);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.linkedin.com/in/jayesh-ganatra-76051056"));
                context.startActivity(intent);
            }
        });

        ImageButton elink = dialog.findViewById(R.id.elink);
        elink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://flowcv.me/jayesh-ganatra"));
                context.startActivity(intent);
            }
        });

        ImageButton glink = dialog.findViewById(R.id.glink);
        glink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/dev?id=7560962222107226464"));
                context.startActivity(intent);
            }
        });
        FloatingActionButton closeInfoBtn = dialog.findViewById(R.id.floatCloseInfoBtn);
        closeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        return dialog;
    }
}