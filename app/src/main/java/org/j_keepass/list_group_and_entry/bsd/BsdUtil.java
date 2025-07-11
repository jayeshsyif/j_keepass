package org.j_keepass.list_group_and_entry.bsd;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
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
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.util.Utils;
import org.j_keepass.util.confirm_alert.ConfirmNotifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BsdUtil {
    public void showGroupEntryMoreOptionsMenu(Context context, Activity activity, String name) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.list_groups_entries_more_option_list);
        TextView groupEntryMenuText = bsd.findViewById(R.id.groupEntryMenuText);
        if (groupEntryMenuText != null) {
            groupEntryMenuText.setText(name);
        }
        LinearLayout groupEntryMoreOptionExportDb = bsd.findViewById(R.id.groupEntryMoreOptionExportDb);
        if (groupEntryMoreOptionExportDb != null) {
            groupEntryMoreOptionExportDb.setOnClickListener(view -> {
                bsd.dismiss();
                PermissionEventSource.getInstance().checkAndGetPermissionReadWriteStorage(view, activity, PermissionEvent.PermissionAction.EXPORT);
            });
        }
        LinearLayout groupEntryMoreOptionShareDb = bsd.findViewById(R.id.groupEntryMoreOptionShareDb);
        if (groupEntryMoreOptionShareDb != null) {
            groupEntryMoreOptionShareDb.setOnClickListener(view -> {
                bsd.dismiss();
                PermissionEventSource.getInstance().checkAndGetPermissionReadWriteStorage(view, activity, PermissionEvent.PermissionAction.SHARE);
            });
        }

        LinearLayout groupEntryMoreOptionGenerateNewPassword = bsd.findViewById(R.id.groupEntryMoreOptionGenerateNewPassword);
        if (groupEntryMoreOptionGenerateNewPassword != null) {
            groupEntryMoreOptionGenerateNewPassword.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.generatingNewPwd));
                    LoadingEventSource.getInstance().showLoading();
                });
                executor.execute(() -> GenerateNewPasswordEventSource.getInstance().generateNewPwd());
            });
        }
        LinearLayout groupEntryMoreOptionChangePwd = bsd.findViewById(R.id.groupEntryMoreOptionChangePwd);
        if (groupEntryMoreOptionChangePwd != null) {
            groupEntryMoreOptionChangePwd.setOnClickListener(view -> {
                bsd.dismiss();
                showAskForChangePassword(context, name, activity);
            });
        }
        LinearLayout groupEntryMoreOptionLock = bsd.findViewById(R.id.groupEntryMoreOptionLock);
        if (groupEntryMoreOptionLock != null) {
            groupEntryMoreOptionLock.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.locking));
                    LoadingEventSource.getInstance().showLoading();
                    Utils.sleepFor3MSec();
                    Db.getInstance().deSetDatabase();
                    ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.LOCK);
                });
            });
        }
        LinearLayout groupEntryMoreOptionEditGroup = bsd.findViewById(R.id.groupEntryMoreOptionEditGroup);
        if (groupEntryMoreOptionEditGroup != null) {
            groupEntryMoreOptionEditGroup.setOnClickListener(view -> {
                bsd.dismiss();
                showAskForEditGroupName(context, activity, name);
            });
        }
        LinearLayout groupEntryMoreOptionCopyOrMoveGroup = bsd.findViewById(R.id.groupEntryMoreOptionCopyOrMoveGroup);
        if (groupEntryMoreOptionCopyOrMoveGroup != null) {
            groupEntryMoreOptionCopyOrMoveGroup.setOnClickListener(view -> {
                bsd.dismiss();
                showAskForCopyOrMove(context, activity, name);
            });
        }
        LinearLayout groupEntryMoreOptionAdd = bsd.findViewById(R.id.groupEntryMoreOptionAdd);
        if (groupEntryMoreOptionAdd != null) {
            groupEntryMoreOptionAdd.setOnClickListener(view -> {
                bsd.dismiss();
                showAskForAddNewItems(context, activity, name);
            });
        }
        LinearLayout groupEntryMoreOptionDeleteGroup = bsd.findViewById(R.id.groupEntryMoreOptionDeleteGroup);
        if (groupEntryMoreOptionDeleteGroup != null) {
            groupEntryMoreOptionDeleteGroup.setOnClickListener(view -> {
                bsd.dismiss();
                new org.j_keepass.util.confirm_alert.BsdUtil().show(view.getContext(), new ConfirmNotifier() {
                    @Override
                    public void onYes() {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.deleting));
                            LoadingEventSource.getInstance().showLoading();
                            if (Db.getInstance().isCurrentGroupRootGroup()) {
                                LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.canNotDeleteRoot));
                            } else {
                                Db.getInstance().deleteGroup(Db.getInstance().getCurrentGroupId(), activity.getContentResolver());
                                ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
                            }
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

    private void showAskForCopyOrMove(Context context, Activity activity, String name) {
        Utils.log("Copy move for " + name + "With type Group");
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
                    Db.getInstance().copyGroup(Db.getInstance().getCurrentGroupId(), adapter.getSelectedGid(), activity);
                    Db.getInstance().setCurrentGroupId(adapter.getSelectedGid());
                    ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
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
                        Db.getInstance().moveGroup(Db.getInstance().getCurrentGroupId(), adapter.getSelectedGid(), activity);
                        Db.getInstance().setCurrentGroupId(adapter.getSelectedGid());
                        ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
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

    public void showAskForAddNewItems(Context context, Activity activity, String name) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.add_more_option_list);
        TableRow addMoreMoreAddNewGroup = bsd.findViewById(R.id.addMoreMoreAddNewGroup);
        if (addMoreMoreAddNewGroup != null) {
            addMoreMoreAddNewGroup.setOnClickListener(view -> {
                bsd.dismiss();
                showAskFoAddGroup(context, activity, name);
            });
        }
        TableRow addMoreMoreAddNewEntry = bsd.findViewById(R.id.addMoreMoreAddNewEntry);
        if (addMoreMoreAddNewEntry != null) {
            addMoreMoreAddNewEntry.setOnClickListener(view -> {
                bsd.dismiss();
                Db.getInstance().getAndSetNewEntry(Db.getInstance().getCurrentGroupId());
                ChangeActivityEventSource.getInstance().changeActivity(ChangeActivityEvent.ChangeActivityAction.ENTRY_NEW);
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    private void showAskFoAddGroup(Context context, Activity activity, String name) {
        Utils.log("show ask for add group for name " + name);
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.add_or_edit_group);
        TextInputEditText groupAddEditName = bsd.findViewById(R.id.groupAddEditName);
        MaterialButton addEditGroupNameBtn = bsd.findViewById(R.id.addEditGroupNameBtn);
        if (addEditGroupNameBtn != null) {
            addEditGroupNameBtn.setText(context.getString(R.string.add));
            addEditGroupNameBtn.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.adding));
                    LoadingEventSource.getInstance().showLoading();
                    if (groupAddEditName == null || groupAddEditName.getText() == null || groupAddEditName.getText().toString().length() == 0) {
                        LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.emptyGroupName));
                    } else {
                        try {
                            Db.getInstance().addGroup(Db.getInstance().getCurrentGroupId(), activity.getContentResolver(), groupAddEditName.getText().toString());
                            ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
                        } catch (Throwable t) {
                            LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.canNotEditGroupName));
                        }
                    }
                });
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    private void showAskForEditGroupName(Context context, Activity activity, String name) {
        Utils.log("Show ask for edit group name for name " + name);
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.add_or_edit_group);
        TextInputEditText groupAddEditName = bsd.findViewById(R.id.groupAddEditName);
        if (groupAddEditName != null) {
            groupAddEditName.setText(Db.getInstance().getGroupName(Db.getInstance().getCurrentGroupId()));
        }
        MaterialButton addEditGroupNameBtn = bsd.findViewById(R.id.addEditGroupNameBtn);
        if (addEditGroupNameBtn != null) {
            addEditGroupNameBtn.setText(context.getString(R.string.change));
            addEditGroupNameBtn.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.changing));
                    LoadingEventSource.getInstance().showLoading();
                    if (groupAddEditName == null || groupAddEditName.getText() == null || groupAddEditName.getText().toString().length() == 0) {
                        LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.emptyGroupName));
                    } else {
                        try {
                            Db.getInstance().updateGroupName(Db.getInstance().getCurrentGroupId(), activity.getContentResolver(), groupAddEditName.getText().toString());
                            ReloadEventSource.getInstance().reload(ReloadEvent.ReloadAction.GROUP_UPDATE);
                        } catch (Throwable t) {
                            LoadingEventSource.getInstance().updateLoadingText(context.getString(R.string.canNotEditGroupName));
                        }
                    }
                });
            });
        }
        expandBsd(bsd);
        bsd.show();
    }

    private void showAskForChangePassword(Context context, String name, Activity activity) {
        Utils.log("Show ask for change password for name " + name);
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.change_pwd);
        TextInputEditText oldPwd = bsd.findViewById(R.id.dbOldPwd);
        TextInputEditText newPwd = bsd.findViewById(R.id.dbNewPwd);
        MaterialButton change = bsd.findViewById(R.id.changePwdBtn);
        if (change != null) {
            change.setOnClickListener(view -> {
                bsd.dismiss();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.changing));
                    LoadingEventSource.getInstance().showLoading();
                    if (oldPwd == null || oldPwd.getText() == null || oldPwd.getText().toString().length() == 0) {
                        LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.enterOldPwd));
                    } else if (newPwd == null || newPwd.getText() == null || newPwd.getText().toString().length() == 0) {
                        LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.enterNewPwd));
                    } else {
                        if (Db.getInstance().pwdMatch(oldPwd.getText().toString())) {
                            Utils.sleepFor3MSec();
                            Db.getInstance().updateDb(activity.getContentResolver(), newPwd.getText().toString().getBytes(StandardCharsets.UTF_8));
                            LoadingEventSource.getInstance().dismissLoading();
                        } else {
                            LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.pwdNotMatch));
                        }
                    }
                });
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
