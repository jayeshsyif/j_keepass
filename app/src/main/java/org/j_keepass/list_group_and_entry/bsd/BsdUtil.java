package org.j_keepass.list_group_and_entry.bsd;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
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
import org.j_keepass.events.newpwd.GenerateNewPasswordEventSource;
import org.j_keepass.events.permission.PermissionEvent;
import org.j_keepass.events.permission.PermissionEventSource;
import org.j_keepass.events.reload.ReloadEvent;
import org.j_keepass.events.reload.ReloadEventSource;
import org.j_keepass.util.Utils;
import org.j_keepass.util.confirm_alert.ConfirmNotifier;

import java.nio.charset.StandardCharsets;
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

    public void showAskForAddNewItems(Context context, Activity activity, String name) {
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.add_more_option_list);
        TableRow addMoreMoreAddNewGroup = bsd.findViewById(R.id.addMoreMoreAddNewGroup);
        addMoreMoreAddNewGroup.setOnClickListener(view -> {
            bsd.dismiss();
            showAskFoAddGroup(context, activity, name);
        });
        TableRow addMoreMoreAddNewEntry = bsd.findViewById(R.id.addMoreMoreAddNewEntry);

        expandBsd(bsd);
        bsd.show();
    }

    private void showAskFoAddGroup(Context context, Activity activity, String name) {
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
                    if (groupAddEditName != null && groupAddEditName.getText() == null || groupAddEditName.getText().toString() == null || groupAddEditName.getText().toString().length() == 0) {
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
                    if (groupAddEditName.getText() == null || groupAddEditName.getText().toString() == null || groupAddEditName.getText().toString().length() == 0) {
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
        final BottomSheetDialog bsd = new BottomSheetDialog(context);
        bsd.setContentView(R.layout.change_pwd);
        TextInputEditText oldPwd = bsd.findViewById(R.id.dbOldPwd);
        TextInputEditText newPwd = bsd.findViewById(R.id.dbNewPwd);
        MaterialButton change = bsd.findViewById(R.id.changePwdBtn);
        change.setOnClickListener(view -> {
            bsd.dismiss();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.changing));
                LoadingEventSource.getInstance().showLoading();
                if (oldPwd.getText() == null || oldPwd.getText().toString() == null || oldPwd.getText().toString().length() == 0) {
                    LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.enterOldPassword));
                } else if (newPwd.getText() == null || newPwd.getText().toString() == null || newPwd.getText().toString().length() == 0) {
                    LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.enterNewPassword));
                } else {
                    if (Db.getInstance().pwdMatch(oldPwd.getText().toString())) {
                        Utils.sleepFor3MSec();
                        Db.getInstance().updateDb(activity.getContentResolver(), newPwd.getText().toString().getBytes(StandardCharsets.UTF_8));
                        LoadingEventSource.getInstance().dismissLoading();
                    } else {
                        LoadingEventSource.getInstance().updateLoadingText(view.getContext().getString(R.string.passwordNotMatch));
                    }
                }
            });
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
