package org.j_keepass.util.confirm_alert;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.j_keepass.R;

public class BsdUtil {
    public void show(Context context, ConfirmNotifier notifier) {
        show(context, null, notifier);
    }

    public void show(Context context, String customText, ConfirmNotifier notifier) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.confirm_alert);
        TextView confirmAlertText = bsd.findViewById(R.id.confirmAlertText);
        if (confirmAlertText != null && customText != null) {
            confirmAlertText.setText(customText);
        }
        MaterialButton yes = bsd.findViewById(R.id.confirmYes);
        if (yes != null) {
            yes.setOnClickListener(v -> {
                bsd.dismiss();
                notifier.onYes();
            });
        }
        MaterialButton no = bsd.findViewById(R.id.confirmNo);
        if (no != null) {
            no.setOnClickListener(v -> {
                bsd.dismiss();
                notifier.onNo();
            });
        }
        ImageButton cancel = bsd.findViewById(R.id.confirmCancel);
        if (cancel != null) {
            cancel.setOnClickListener(v -> bsd.dismiss());
        }
        expandBsd(bsd);
        bsd.show();
    }

    private void expandBsd(BottomSheetDialog bsd) {
        FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
            bottomSheetBehavior.setSkipCollapsed(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
}
