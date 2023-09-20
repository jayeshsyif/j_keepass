package org.j_keepass.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.R;

public class InfoDialogUtil {

    public void showInfoDialog(Context context) {
        BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.info_layout);
        bsd.show();

        ImageButton link = bsd.findViewById(R.id.llink);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.linkedin.com/in/jayesh-ganatra-76051056"));
                context.startActivity(intent);
            }
        });

        ImageButton elink = bsd.findViewById(R.id.elink);
        elink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://flowcv.me/jayesh-ganatra"));
                context.startActivity(intent);
            }
        });

        ImageButton glink = bsd.findViewById(R.id.glink);
        glink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsd.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/dev?id=7560962222107226464"));
                context.startActivity(intent);
            }
        });

        bsd.findViewById(R.id.changeLogsLinearLayout).setVisibility(View.GONE);
        ((AppCompatImageButton) bsd.findViewById(R.id.changeLogShowHide)).setOnClickListener(v -> {
            if (bsd.findViewById(R.id.changeLogsLinearLayout).getVisibility() == View.GONE) {
                bsd.findViewById(R.id.changeLogsLinearLayout).setVisibility(View.VISIBLE);
                ((AppCompatImageButton) bsd.findViewById(R.id.changeLogShowHide)).setImageResource(R.drawable.ic_visibility_off_fill0_wght300_grad_25_opsz24);
            } else {
                bsd.findViewById(R.id.changeLogsLinearLayout).setVisibility(View.GONE);
                ((AppCompatImageButton) bsd.findViewById(R.id.changeLogShowHide)).setImageResource(R.drawable.ic_visibility_fill0_wght300_grad_25_opsz24);
            }
        });
    }
}
