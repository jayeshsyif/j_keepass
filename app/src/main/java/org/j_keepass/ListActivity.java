package org.j_keepass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityListBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.ConfirmDialogUtil;
import org.j_keepass.util.PasswordGenerator;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.SearchDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ActivityListBinding binding;
    private boolean isSearchView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.floatAdd.shrink();

        if (Common.database == null) {
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        } else {

            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            ProgressDialogUtil.setLoadingProgress(alertDialog, 10);
            runOnUiThread(() -> {
                Database<?, ?, ?, ?> database = Common.database;
                Group<?, ?, ?, ?> group = Common.group;

                ProgressDialogUtil.setLoadingProgress(alertDialog, 20);
                if (group == null) {
                    group = database.getRootGroup();
                    Common.group = group;
                }

                if (group != null) {
                    listAndShowGroupsAndEntries(group, false, alertDialog);
                    ProgressDialogUtil.setLoadingProgress(alertDialog, 50);

                }
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);

            });

            binding.floatAdd.setOnClickListener(v -> {
                if (!binding.floatAdd.isExtended()) {
                    binding.floatAdd.extend();
                } else {
                    binding.floatAdd.shrink();
                }
                AlertDialog alertDialog1 = getDialog();
                alertDialog1.show();
            });

        }

        binding.searchFloatBtn.setOnClickListener(v -> {
            search(v, this);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @SuppressLint("ResourceType")
    private void listAndShowGroupsAndEntries(Group<?, ?, ?, ?> group, boolean isFromBack, AlertDialog alertDialog) {
        if (alertDialog == null) {
            final AlertDialog alertDialogLocal = ProgressDialogUtil.getLoading(getLayoutInflater(), ListActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialogLocal);
            ProgressDialogUtil.setLoadingProgress(alertDialogLocal, 10);
            runOnUiThread(() -> {
                binding.groupName.setText(group.getName());
                if (!isFromBack) {
                    binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_slide_in_left));
                } else {
                    binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_slide_in_right));
                }
                binding.groupScrollLinearLayout.removeAllViews();
                binding.groupScrollView.fullScroll(View.FOCUS_UP);
                ProgressDialogUtil.setLoadingProgress(alertDialogLocal, 20);
                for (Group<?, ?, ?, ?> g : group.getGroups()) {
                    addGroupOnUi(g, isFromBack);
                }
                ProgressDialogUtil.setLoadingProgress(alertDialogLocal, 50);
                for (Entry<?, ?, ?, ?> e : group.getEntries()) {
                    addEntryOnUi(e, isFromBack, false);
                }
                ProgressDialogUtil.setLoadingProgress(alertDialogLocal, 100);
                ProgressDialogUtil.dismissLoadingDialog(alertDialogLocal);
            });
        }
        binding.groupName.setText(group.getName());
        if (!isFromBack) {
            binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_slide_in_left));
        } else {
            binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_slide_in_right));
        }
        binding.groupScrollLinearLayout.removeAllViews();
        binding.groupScrollView.fullScroll(View.FOCUS_UP);
        for (Group<?, ?, ?, ?> g : group.getGroups()) {
            addGroupOnUi(g, isFromBack);
        }
        for (Entry<?, ?, ?, ?> e : group.getEntries()) {
            addEntryOnUi(e, isFromBack, false);
        }
    }

    private AlertDialog getDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(ListActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.add_new_layout, null);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.getWindow().setGravity(Gravity.CENTER);

        ScrollView newFloatScrollView = mView.findViewById(R.id.newFloatScrollView);
        newFloatScrollView.setAnimation(AnimationUtils.makeInAnimation(this, true));

        FloatingActionButton closeInfoBtn = mView.findViewById(R.id.addNewfloatCloseInfoBtn);
        closeInfoBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            if (!binding.floatAdd.isExtended()) {
                binding.floatAdd.extend();
            } else {
                binding.floatAdd.shrink();
            }
        });
        MaterialButton addGroup = mView.findViewById(R.id.addNewGroupfloatBtn);
        addGroup.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(ListActivity.this, AddGroupActivity.class);
            startActivity(intent);
            finish();
        });

        MaterialButton addEntry = mView.findViewById(R.id.addNewEntryfloatBtn);
        addEntry.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(ListActivity.this, AddEntryActivity.class);
            startActivity(intent);
            finish();
        });

        LinearLayout randomPasswordLayout = mView.findViewById(R.id.randomPasswordLayout);
        TextInputEditText randomPassword = mView.findViewById(R.id.randomPassword);
        ImageButton randomPasswordCopy = mView.findViewById(R.id.randomPasswordCopy);
        MaterialButton newRandonPasswordFloatBtn = mView.findViewById(R.id.newRandonPasswordFloatBtn);
        newRandonPasswordFloatBtn.setOnClickListener(v -> {
            if (randomPasswordLayout.getVisibility() == View.GONE) {
                randomPassword.setText(new PasswordGenerator().generate(20));
                randomPasswordLayout.setVisibility(View.VISIBLE);
                randomPasswordLayout.startAnimation(AnimationUtils.makeInAnimation(mView.getContext(), true));
            } else {
                randomPasswordLayout.setVisibility(View.GONE);
            }
        });
        randomPasswordCopy.setOnClickListener(v -> {
            if (randomPassword.getText() != null) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("password", randomPassword.getText().toString());
                clipboard.setPrimaryClip(clip);
                ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
            }
        });
        return alertDialog;
    }

    @Override
    public void onBackPressed() {
        if (Common.group != null && !Common.group.isRootGroup()) {
            Common.group = Common.group.getParent();
            listAndShowGroupsAndEntries(Common.group, true, null);
        } else if( Common.group != null && isSearchView){
            listAndShowGroupsAndEntries(Common.group, true, null);
        }else {
            super.onBackPressed();
            Intent intent = new Intent(ListActivity.this, LoadActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("ResourceType")
    private void addGroupOnUi(Group<?, ?, ?, ?> g, boolean isFromBack) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_adapter_view, null);
        ((TextView) viewToLoad.findViewById(R.id.adapterText)).setText(g.getName());
        viewToLoad.setOnClickListener(v -> {
            Common.group = g;
            isSearchView = false;
            listAndShowGroupsAndEntries(g, false, null);
        });
        ImageView edit = viewToLoad.findViewById(R.id.editGroupBtn);
        edit.setOnClickListener(v -> {
            Common.group = g;
            Intent intent = new Intent(this, EditGroupActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "group");
            intent.putExtras(bundle);
            this.startActivity(intent);
            this.finish();
        });
        ImageView delete = viewToLoad.findViewById(R.id.deleteGroupBtn);
        delete.setOnClickListener(v -> {
            deleteGroup(v, this, g);
        });
        if (!isFromBack) {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_left), 0.5f);
            binding.groupScrollLinearLayout.setLayoutAnimation(lac);
        } else {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_right), 0.5f);
            binding.groupScrollLinearLayout.setLayoutAnimation(lac);
        }
        binding.groupScrollLinearLayout.addView(viewToLoad);
    }

    @SuppressLint("ResourceType")
    private void addEntryOnUi(Entry<?, ?, ?, ?> e, boolean isFromBack, boolean showGroupInfo) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_adapter_view, null);
        ((TextView) viewToLoad.findViewById(R.id.adapterText)).setText(e.getTitle());
        ShapeableImageView adapterIconImageView = viewToLoad.findViewById(R.id.adapterIconImageView);
        adapterIconImageView.setImageResource(R.drawable.ic_key_fill0_wght300_grad_25_opsz24);
        viewToLoad.setOnClickListener(v -> {
            Common.entry = e;
            Intent intent = new Intent(this, ViewEntryActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "entry");
            intent.putExtras(bundle);
            this.startActivity(intent);
            this.finish();
        });
        ImageView edit = viewToLoad.findViewById(R.id.editGroupBtn);
        edit.setOnClickListener(v -> {
            Common.entry = e;
            Intent intent = new Intent(this, EditEntryActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "entry");
            intent.putExtras(bundle);
            this.startActivity(intent);
            this.finish();
        });
        ImageView delete = viewToLoad.findViewById(R.id.deleteGroupBtn);
        delete.setOnClickListener(v -> {
            deleteEntry(v, this, e);
        });
        if (showGroupInfo) {
            TextView adapterGroupInfo = viewToLoad.findViewById(R.id.adapterGroupInfo);
            String path = e.getPath();
            if (path != null) {
                path = path.substring(1, path.length());
                path = path.replace("/", " \u002D ");
                adapterGroupInfo.setVisibility(View.VISIBLE);
                adapterGroupInfo.setText(path);
            }
        }
        if (!isFromBack) {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_left), 0.5f);
            binding.groupScrollLinearLayout.setLayoutAnimation(lac);
        } else {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_right), 0.5f);
            binding.groupScrollLinearLayout.setLayoutAnimation(lac);
        }
        binding.groupScrollLinearLayout.addView(viewToLoad);
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
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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
                            //activity.getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
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

    @SuppressLint("ResourceType")
    private void search(View v, Activity activity) {
        Triplet<AlertDialog, MaterialButton, TextInputEditText> searchDialog = SearchDialogUtil.getSearchDialog(activity.getLayoutInflater(), activity);
        searchDialog.second.setOnClickListener(viewObj -> {
            searchDialog.first.dismiss();
            final AlertDialog alertDialog = ProgressDialogUtil.getSearch(activity.getLayoutInflater(), activity);
            ProgressDialogUtil.showSearchDialog(alertDialog);
            runOnUiThread(() -> {
                isSearchView = true;
                binding.groupScrollLinearLayout.removeAllViews();
                binding.groupScrollView.fullScroll(View.FOCUS_UP);
                binding.groupName.setText(getString(R.string.search) + ": " + searchDialog.third.getText().toString());
                binding.groupName.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_slide_in_left));
                List<?> searchedEntries = Common.database.findEntries(searchDialog.third.getText().toString());
                for (int eCount = 0; eCount < searchedEntries.size(); eCount++) {
                    Entry<?, ?, ?, ?> localEntry = (Entry<?, ?, ?, ?>) searchedEntries.get(eCount);
                    addEntryOnUi(localEntry, false, true);
                }
                ProgressDialogUtil.dismissSearchDialog(alertDialog);
            });
        });
        ConfirmDialogUtil.showDialog(searchDialog.first);
    }
}