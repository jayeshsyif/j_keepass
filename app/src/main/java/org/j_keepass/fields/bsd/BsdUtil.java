package org.j_keepass.fields.bsd;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.changeactivity.ChangeActivityEvent;
import org.j_keepass.events.changeactivity.ChangeActivityEventSource;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.util.confirm_alert.ConfirmNotifier;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BsdUtil {
    public void showAddNewProperty(Context context, UUID eId) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.add_additional_property);
        TextInputEditText additionalPropertyFieldValue = bsd.findViewById(R.id.additionalPropertyFieldValue);
        TextInputEditText additionalPropertyFieldName = bsd.findViewById(R.id.additionalPropertyFieldName);
        MaterialButton saveAdditionalPropertyBtn = bsd.findViewById(R.id.saveAdditionalPropertyBtn);
        if (saveAdditionalPropertyBtn != null) {
            saveAdditionalPropertyBtn.setOnClickListener(view -> {
                if (additionalPropertyFieldValue == null || additionalPropertyFieldValue.getText() == null || additionalPropertyFieldValue.getText().toString().length() == 0) {
                    if (additionalPropertyFieldValue != null) {
                        additionalPropertyFieldValue.requestFocus();
                    }
                } else if (additionalPropertyFieldName == null || additionalPropertyFieldName.getText() == null || additionalPropertyFieldName.getText().toString().length() == 0) {
                    if (additionalPropertyFieldName != null) {
                        additionalPropertyFieldName.requestFocus();
                    }
                } else {
                    hideKeyboard(view);
                    bsd.dismiss();
                    String addingStr = view.getContext().getString(R.string.adding);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        LoadingEventSource.getInstance().updateLoadingText(addingStr);
                        LoadingEventSource.getInstance().showLoading();
                        Db.getInstance().addEntryAdditionalProperty(eId, additionalPropertyFieldName.getText().toString(), additionalPropertyFieldValue.getText().toString());
                        ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.ENTRY_PROP_UPDATE);
                    });
                }
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

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showMoreOptions(Context context, String eTitle, boolean isEditOrIsNew, Activity activity) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.selected_entry_more_option_list);
        TextView selectedEntryMenuText = bsd.findViewById(R.id.selectedEntryMenuText);
        if (selectedEntryMenuText != null) {
            selectedEntryMenuText.setText(eTitle);
        }
        LinearLayout selectedEntryMoreOptionEdit = bsd.findViewById(R.id.selectedEntryMoreOptionEdit);
        if (selectedEntryMoreOptionEdit != null) {
            if (isEditOrIsNew) {
                selectedEntryMoreOptionEdit.setVisibility(View.GONE);
            } else {
                selectedEntryMoreOptionEdit.setOnClickListener(view -> {
                    bsd.dismiss();
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.opening));
                        LoadingEventSource.getInstance().showLoading();
                        ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_SELECTED_FOR_EDIT);
                    });
                });
            }
        }
        LinearLayout selectedEntryMoreOptionDelete = bsd.findViewById(R.id.selectedEntryMoreOptionDelete);
        if (selectedEntryMoreOptionDelete != null) {
            selectedEntryMoreOptionDelete.setOnClickListener(view -> {
                bsd.dismiss();
                new org.j_keepass.util.confirm_alert.BsdUtil().show(view.getContext(), new ConfirmNotifier() {
                    @Override
                    public void onYes() {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.deleting));
                            LoadingEventSource.getInstance().showLoading();
                            Db.getInstance().deleteEntry(Db.getInstance().getCurrentEntryId(), activity.getContentResolver());
                            ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_DELETED);
                        });
                    }

                    @Override
                    public void onNo() {
                        // ignore
                    }
                });
            });
        }
        expandBsd(bsd);
        bsd.show();
    }
}
