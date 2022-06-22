package org.j_keepass.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.j_keepass.EditEntryActivity;
import org.j_keepass.EditGroupActivity;
import org.j_keepass.ListActivity;
import org.j_keepass.R;
import org.j_keepass.ViewEntryActivity;
import org.j_keepass.util.Common;
import org.j_keepass.util.ConfirmDialogUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;
import java.util.ArrayList;

public class ListGroupAdapter extends BaseAdapter {
    private final ArrayList<Pair<Object, Boolean>> pairs;
    private final Activity activity;

    public ListGroupAdapter(ArrayList<Pair<Object, Boolean>> pairs, Activity activity) {
        this.pairs = pairs;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return pairs.size();
    }

    @Override
    public Pair<Object, Boolean> getItem(int position) {
        if (pairs != null) {
            return pairs.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(activity).inflate(R.layout.activity_list_adapter_view, parent, false);
        TextView tx = convertView.findViewById(R.id.adapterText);
        //LinearLayout mainIL = convertView.findViewById(R.id.adapterMainLinearLayout);
        ShapeableImageView adapterIconImageView = convertView.findViewById(R.id.adapterIconImageView);
        ImageView edit = convertView.findViewById(R.id.editGroupBtn);
        ImageView delete = convertView.findViewById(R.id.deleteGroupBtn);
        Pair<Object, Boolean> pair = getItem(position);
        if (pair != null && tx != null) {
            if (pair.second) {
                //its group
                Group<?, ?, ?, ?> localGroup = (Group<?, ?, ?, ?>) pair.first;
                tx.setText(localGroup.getName());
                tx.setOnClickListener(v -> {
                    Common.group = localGroup;
                    Intent intent = new Intent(activity, ListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("click", "group");
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                    activity.finish();
                });
                edit.setOnClickListener(v -> {
                    Common.group = localGroup;
                    Intent intent = new Intent(activity, EditGroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("click", "group");
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                    activity.finish();
                });
                delete.setOnClickListener(v -> deleteGroup(v, activity, localGroup));
            } else {
                Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) pair.first;
                tx.setText(localEntry.getTitle());
                //mainIL.setBackgroundColor(convertView.getResources().getColor(R.color.kp_green_2));
                adapterIconImageView.setImageResource(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
                tx.setOnClickListener(v -> {
                    Common.entry = localEntry;
                    Intent intent = new Intent(activity, ViewEntryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("click", "entry");
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                    activity.finish();
                });
                edit.setOnClickListener(v -> {
                    Common.entry = localEntry;
                    Intent intent = new Intent(activity, EditEntryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("click", "entry");
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                    activity.finish();
                });
                delete.setOnClickListener(v -> deleteEntry(v, activity, localEntry));
            }
        }
        return convertView;
    }

    private void deleteGroup(View v, Activity activity, Group group) {

        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {

            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);

            new Thread(() -> {
                String groupName = null;
                Group parent = Common.group;

                if (parent == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Parent is null");
                } else {
                    parent.removeGroup(group);
                    Common.group = parent;
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage());
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage());
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted);
                    }
                }
            }).start();
        });
        ConfirmDialogUtil.showDialog(confirmDialog.first);
    }

    private void deleteEntry(View v, Activity activity, Entry entry) {

        Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(activity.getLayoutInflater(), activity);
        confirmDialog.second.setOnClickListener(viewObj -> {


            final AlertDialog alertDialog = ProgressDialogUtil.getSaving(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSavingDialog(alertDialog);

            new Thread(() -> {
                String groupName = null;
                Group parent = Common.group;

                if (parent == null) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(activity.getLayoutInflater(), v, "Group is null");
                } else {
                    parent.removeEntry(entry);
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                        OutputStream fileOutputStream = null;
                        try {
                            activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                            fileOutputStream = activity.getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                            ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                            Common.database.save(Common.creds, fileOutputStream);
                            ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                            Intent intent = new Intent(activity, ListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("click", "group");
                            intent.putExtras(bundle);
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (NoSuchMethodError e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage());
                        } catch (Exception e) {
                            ProgressDialogUtil.dismissSavingDialog(alertDialog);
                            ToastUtil.showToast(activity.getLayoutInflater(), v, e.getMessage());
                        } finally {
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (Exception e) {
                                    //do nothing
                                }
                            }
                        }
                    } else {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(activity.getLayoutInflater(), v, R.string.permissionNotGranted);
                    }
                }
            }).start();
        });
        confirmDialog.first.show();
    }
}
