package org.j_keepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.AddNewGroupLayoutBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ThemeSettingDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;


public class AddGroupActivity extends AppCompatActivity {

    private AddNewGroupLayoutBinding binding;
    private static final int READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeSettingDialogUtil.onActivityCreateSetTheme(this, false);
        binding = AddNewGroupLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(AddGroupActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {

           /* LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
            binding.addGroupScrollView.setLayoutAnimation(lac);
            binding.addGroupScrollView.startLayoutAnimation();*/
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final Pair<View, TextInputEditText> pair = new FieldUtil().getEditTextField(inflater, getString(R.string.enterGroupName), "");

            pair.second.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (pair.second.getText() != null) {
                        binding.addGroupTitleName.setText(pair.second.getText().toString());
                    }
                }
            });
            binding.addGroupScrollViewLinearLayout.addView(pair.first);

            binding.saveGroup.setOnClickListener(v -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), AddGroupActivity.this);
                ProgressDialogUtil.showSavingDialog(alertDialog);
                new Thread(() -> {

                    boolean proceed = false;
                    try {
                        validate(pair.second.getText().toString());
                        proceed = true;
                    } catch (KpCustomException e) {
                        ProgressDialogUtil.dismissSavingDialog(alertDialog);
                        ToastUtil.showToast(getLayoutInflater(), v, e,binding.getRoot().findViewById(R.id.saveGroup));
                    }

                    if (proceed) {
                        Group group = Common.group;
                        Group newGroup = Common.database.newGroup();
                        newGroup.setName(pair.second.getText().toString());
                        newGroup.setParent(group);
                        group.addGroup(newGroup);
                        boolean isOk = checkAndGetPermission(v, AddGroupActivity.this);

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
                                Intent intent = new Intent(AddGroupActivity.this, ListActivity.class);
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
                Intent intent = new Intent(AddGroupActivity.this, LoadActivity.class);
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
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted,binding.getRoot().findViewById(R.id.saveGroup));
            }
        } else {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity, new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES},
                        READ_EXTERNAL_STORAGE);
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