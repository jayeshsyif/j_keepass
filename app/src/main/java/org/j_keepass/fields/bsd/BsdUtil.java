package org.j_keepass.fields.bsd;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.R;
import org.j_keepass.copy_or_move.adapter.ListGroupsForCopyMoveAdapter;
import org.j_keepass.db.operation.Db;
import org.j_keepass.events.changeactivity.ChangeActivityEvent;
import org.j_keepass.events.changeactivity.ChangeActivityEventSource;
import org.j_keepass.events.loading.LoadingEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.util.Utils;
import org.j_keepass.util.confirm_alert.ConfirmNotifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        LinearLayout selectedEntryMoreOptionCopyMove = bsd.findViewById(R.id.selectedEntryMoreOptionCopyMove);
        if (selectedEntryMoreOptionCopyMove != null) {
            selectedEntryMoreOptionCopyMove.setOnClickListener(view -> {
                bsd.dismiss();
                showAskForCopyOrMove(context, activity, eTitle);
            });
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
        LinearLayout selectedEntryMoreOptionShare = bsd.findViewById(R.id.selectedEntryMoreOptionShare);
        if (selectedEntryMoreOptionShare != null) {
            selectedEntryMoreOptionShare.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_SHARE_AS_TEXT);
                });
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    private void showAskForCopyOrMove(Context context, Activity activity, String eTitle) {
        Utils.log("Copy move for " + eTitle + "With type entry");
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.copy_or_move);
        RecyclerView showFolderForCopyMoveRecyclerView = bsd.findViewById(R.id.showFolderForCopyMoveRecyclerView);
        TextView selectedFolderNameForCopyMove = bsd.findViewById(R.id.selectedFolderNameForCopyMove);
        if (selectedFolderNameForCopyMove != null) {
            selectedFolderNameForCopyMove.setText(context.getString(R.string.copyOrMoveSelectedFolderName, Db.getInstance().getGroupName(Db.getInstance().getRootGroupId())));
        }
        ListGroupsForCopyMoveAdapter adapter = new ListGroupsForCopyMoveAdapter();
        if (showFolderForCopyMoveRecyclerView != null) {
            showFolderForCopyMoveRecyclerView.setItemAnimator(adapter.getItemAnimator());
        }
        ReloadEvent reloadEvent = reloadAction -> {
            if (reloadAction != null && reloadAction.name().equals(ReloadEvent.ReloadAction.COPY_MOVE_GROUP_UPDATE.name())) {
                activity.runOnUiThread(() -> {
                    Utils.log("Copy or move folder update is received.");
                    if (showFolderForCopyMoveRecyclerView != null) {
                        showFolderForCopyMoveRecyclerView.removeAllViews();
                    }
                    if (selectedFolderNameForCopyMove != null) {
                        selectedFolderNameForCopyMove.setText(context.getString(R.string.copyOrMoveSelectedFolderName, adapter.getSelectedGName()));
                    }
                    ArrayList<GroupEntryData> subs = Db.getInstance().getSubGroupsOnly(adapter.getSelectedGid());
                    List<GroupEntryData> prevSubs = adapter.getAll();
                    if (prevSubs != null) {
                        Iterator<GroupEntryData> iterator = prevSubs.iterator();
                        int index = 0;
                        while (iterator.hasNext()) {
                            GroupEntryData d = iterator.next();
                            index++;
                            if (d != null) {
                                iterator.remove();
                            }
                            adapter.notifyItemRemoved(index);
                        }
                    }
                    if (subs != null) {
                        for (final GroupEntryData data : subs) {
                            adapter.addValue(data);
                            adapter.notifyItemInserted(adapter.getItemCount());
                            Utils.sleepFor1MSec();
                        }
                    }
                });
            }
        };
        adapter.setReloadEvent(reloadEvent);
        if (showFolderForCopyMoveRecyclerView != null) {
            activity.runOnUiThread(() -> {
                LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.loading));
                LoadingEventSource.getInstance().showLoading();
                showFolderForCopyMoveRecyclerView.removeAllViews();
                showFolderForCopyMoveRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                showFolderForCopyMoveRecyclerView.setAdapter(adapter);
                adapter.setSelectedGid(Db.getInstance().getRootGroupId());
                ArrayList<GroupEntryData> subs = Db.getInstance().getSubGroupsOnly(Db.getInstance().getRootGroupId());
                if (subs != null) {
                    for (final GroupEntryData data : subs) {
                        adapter.addValue(data);
                        adapter.notifyItemInserted(adapter.getItemCount());
                    }
                    LoadingEventSource.getInstance().dismissLoading();
                }
            });
        }
        MaterialButton confirmCopyHere = bsd.findViewById(R.id.confirmCopyHere);
        if (confirmCopyHere != null) {
            confirmCopyHere.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.copying));
                    LoadingEventSource.getInstance().showLoading();
                    Db.getInstance().copyEntry(Db.getInstance().getCurrentEntryId(), adapter.getSelectedGid(), activity);
                    Db.getInstance().setCurrentGroupId(adapter.getSelectedGid());
                    ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_COPIED_MOVED);
                });
            });
        }
        MaterialButton confirmMoveHere = bsd.findViewById(R.id.confirmMoveHere);
        if (confirmMoveHere != null) {
            confirmMoveHere.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.moving));
                    LoadingEventSource.getInstance().showLoading();
                    if (Db.getInstance().getCurrentGroupId() != Db.getInstance().getRootGroupId()) {
                        Db.getInstance().moveEntry(Db.getInstance().getCurrentEntryId(), adapter.getSelectedGid(), activity);
                        Db.getInstance().setCurrentGroupId(adapter.getSelectedGid());
                        ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_COPIED_MOVED);
                    } else {
                        LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.canNotMoveRootFolder));
                    }
                });
            });
        }
        ImageButton copyOrMoveMenuCancelBtn = bsd.findViewById(R.id.copyOrMoveMenuCancelBtn);
        if (copyOrMoveMenuCancelBtn != null) {
            copyOrMoveMenuCancelBtn.setOnClickListener(view -> bsd.dismiss());
        }
        expandBsd(bsd);
        bsd.show();
    }
}
