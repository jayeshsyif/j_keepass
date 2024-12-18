package org.j_keepass.util.bsd.groupentry;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.j_keepass.R;
import org.j_keepass.groupentry.eventinterface.GroupEntryEventSource;
import org.j_keepass.loading.eventinterface.LoadingEventSource;
import org.j_keepass.newpwd.eventinterface.GenerateNewPasswordEventSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BsdUtil {
    public void showGroupEntryMoreOptionsMenu(Context context, Activity activity, String name) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.group_entry_more_option_list);
        TextView groupEntryMenuText = bsd.findViewById(R.id.groupEntryMenuText);
        if (groupEntryMenuText != null) {
            groupEntryMenuText.setText(name);
        }
        LinearLayout groupEntryMoreOptionGenerateNewPassword = bsd.findViewById(R.id.groupEntryMoreOptionGenerateNewPassword);
        if (groupEntryMoreOptionGenerateNewPassword != null) {
            groupEntryMoreOptionGenerateNewPassword.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPassword));
                    LoadingEventSource.getInstance().showLoading();
                });
                executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
            });
        }
        LinearLayout groupEntryMoreOptionLock = bsd.findViewById(R.id.groupEntryMoreOptionLock);
        if (groupEntryMoreOptionLock != null) {
            groupEntryMoreOptionLock.setOnClickListener(view -> {
                bsd.dismiss();
                GroupEntryEventSource.getInstance().lock();
            });
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
