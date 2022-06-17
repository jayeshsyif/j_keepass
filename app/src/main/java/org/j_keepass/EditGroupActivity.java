package org.j_keepass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.j_keepass.databinding.EditNewGroupLayoutBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;


public class EditGroupActivity extends AppCompatActivity {

    private EditNewGroupLayoutBinding binding;
    private static final int MANAGE_DOCUMENTS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EditNewGroupLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(EditGroupActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {
            if(Common.group != null) {
                binding.addGroupName.setText(Common.group.getName());
                binding.addGroupTitleName.setText(Common.group.getName());
            }
            binding.addGroupName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (binding.addGroupName.getText() != null) {
                        binding.addGroupTitleName.setText(binding.addGroupName.getText().toString());
                    }
                }
            });
            binding.saveGroup.setOnClickListener(v -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), EditGroupActivity.this);
                ProgressDialogUtil.showSavingDialog(alertDialog);

                new Thread(() -> {
                    String groupName = null;
                    if (binding.addGroupName.getText() == null) {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.groupNameEmptyErrorMsg);
                    } else {
                        groupName = binding.addGroupName.getText().toString();
                    }
                    if (groupName == null || groupName.length() <= 0) {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.groupNameEmptyErrorMsg);
                    } else {
                        Group group = Common.group;
                        group.setName(groupName);
                        group.setParent(group.getParent());
                        Common.group = group.getParent();

                        if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EditGroupActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MANAGE_DOCUMENTS);
                        }

                        if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                            OutputStream fileOutputStream = null;
                            try {
                                getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                                fileOutputStream = getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                                ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                                Common.database.save(Common.creds, fileOutputStream);
                                ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                                Intent intent = new Intent(EditGroupActivity.this, ListActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("click", "group");
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            } catch (NoSuchMethodError e) {
                                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage());
                            } catch (Exception e) {
                                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage());
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
                            ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
                        }

                    }
                }).start();
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}