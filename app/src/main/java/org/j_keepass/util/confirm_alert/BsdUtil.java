package org.j_keepass.util.confirm_alert;

import android.content.Context;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.j_keepass.R;

public class BsdUtil {
    public void show(Context context, ConfirmNotifier notifier) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.confirm_alert);
        MaterialButton yes = bsd.findViewById(R.id.confirmYes);
        yes.setOnClickListener(v -> {
            bsd.dismiss();
            notifier.onYes();
        });
        MaterialButton no = bsd.findViewById(R.id.confirmNo);
        no.setOnClickListener(v -> {
            bsd.dismiss();
            notifier.onNo();
        });
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
