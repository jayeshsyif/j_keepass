package org.j_keepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.EditNewGroupLayoutBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;


public class EditGroupActivity extends AppCompatActivity {

    private EditNewGroupLayoutBinding binding;
    private static final int READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = EditNewGroupLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(EditGroupActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {

            Pair<View, TextInputEditText> pair = null;
            if (Common.group != null) {
                binding.editGroupTitleName.setText(Common.group.getName());
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                pair = new FieldUtil().getEditTextField(inflater, getString(R.string.groupName), Common.group.getName());
                binding.editGroupScrollViewLinearLayout.addView(pair.first);
            }
            final Pair<View, TextInputEditText> finalPair = pair;
            binding.saveGroup.setOnClickListener(v -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), EditGroupActivity.this);
                ProgressDialogUtil.showSavingDialog(alertDialog);
                new Thread(() -> {
                    boolean proceed = false;
                    try {
                        validate(finalPair.second.getText().toString());
                        proceed = true;
                    } catch (KpCustomException e) {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(getLayoutInflater(), v, e,binding.getRoot().findViewById(R.id.saveGroup));
                    }

                    if (proceed) {
                        Group group = Common.group;
                        group.setName(finalPair.second.getText().toString());
                        group.setParent(group.getParent());
                        Common.group = group.getParent();

                        boolean isOk = checkAndGetPermission(v, EditGroupActivity.this);

                        if (isOk) {
                            ProgressDialogUtil.setSavingProgress(alertDialog, 30);
                            OutputStream fileOutputStream = null;
                            try {
                                //getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                ProgressDialogUtil.setSavingProgress(alertDialog, 40);
                                Util.sleepForHalfSec();
                                fileOutputStream = getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                                ProgressDialogUtil.setSavingProgress(alertDialog, 50);
                                Common.database.save(Common.creds, fileOutputStream);
                                ProgressDialogUtil.setSavingProgress(alertDialog, 100);
                                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                                Intent intent = new Intent(EditGroupActivity.this, ListActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("click", "group");
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            } catch (NoSuchMethodError e) {
                                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(),binding.getRoot().findViewById(R.id.saveGroup));
                            } catch (Exception e) {
                                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(),binding.getRoot().findViewById(R.id.saveGroup));
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
                            ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted,binding.getRoot().findViewById(R.id.saveGroup));
                        }

                    }
                }).start();
            });
            binding.home.setOnClickListener(v -> {
                Common.group = Common.database.getRootGroup();
                this.onBackPressed();
            });

            binding.lockBtn.setOnClickListener(v -> {
                Common.database = null;
                Common.group = null;
                Common.entry = null;
                Common.creds = null;
                Common.kdbxFileUri = null;
                Intent intent = new Intent(EditGroupActivity.this, LoadActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (Common.group != null) {
            Common.group = Common.group.getParent();
            Intent intent = new Intent(this, ListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("click", "group");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void validate(String groupName) throws KpCustomException {

        if (groupName == null || groupName.length() <= 0) {
            throw new KpCustomException(R.string.groupNameEmptyErrorMsg);
        }
        if (!Common.isCodecAvailable) {
            throw new KpCustomException(R.string.devInProgress);
        }
    }

    private boolean checkAndGetPermission(View v, Activity activity) {
        boolean isOK = false;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted,binding.getRoot().findViewById(R.id.saveGroup));
            }
        } else {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted,binding.getRoot().findViewById(R.id.saveGroup));
            }
        }
        return isOK;
    }
}