package org.j_keepass;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityEditEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.NewPasswordDialogUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Entry;

import java.io.OutputStream;
import java.util.ArrayList;

public class EditEntryActivity extends AppCompatActivity {

    private ActivityEditEntryBinding binding;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private ArrayList<Pair<View, TextInputEditText>> fields = new ArrayList<Pair<View, TextInputEditText>>();
    private ArrayList<Pair<View, TextInputEditText>> fieldsToRemove = new ArrayList<Pair<View, TextInputEditText>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = ActivityEditEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(EditEntryActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {
            Entry<?, ?, ?, ?> entry = null;
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String click = bundle.getString("click");
                if (click != null && click.equalsIgnoreCase("entry")) {
                    entry = Common.entry;
                }
            }

            if (entry != null) {
                binding.editEntryTitleNameHeader.setText(entry.getTitle());
                binding.editBasicFieldEntryScrollViewLinearLayout.removeAllViews();
                binding.editAdditionalFieldEntryScrollViewLinearLayout.removeAllViews();
                ArrayList<View> viewsToAdd = new ArrayList<View>();

                final Pair<View, TextInputEditText> titleView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.entryTitle), entry.getTitle());
                titleView.second.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (titleView.second.getText() != null) {
                            binding.editEntryTitleNameHeader.setText(titleView.second.getText().toString());
                        }
                    }
                });
                viewsToAdd.add(titleView.first);
                fields.add(titleView);

                final Pair<View, TextInputEditText> userNameView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.userName), entry.getUsername());
                viewsToAdd.add(userNameView.first);
                fields.add(userNameView);

                final Pair<View, TextInputEditText> passwordView = new FieldUtil().getEditPasswordField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.password), entry.getPassword());
                viewsToAdd.add(passwordView.first);
                fields.add(passwordView);

                final Pair<View, TextInputEditText> urlView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.url), entry.getUrl());
                viewsToAdd.add(urlView.first);
                fields.add(urlView);

                final Pair<View, TextInputEditText> notesView = new FieldUtil().getMultiEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.notes), entry.getNotes());
                viewsToAdd.add(notesView.first);
                fields.add(notesView);

                final Pair<View, TextInputEditText> expiryDatePicker = new FieldUtil().getDatePickerTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.expiryDate), entry.getExpiryTime());
                viewsToAdd.add(expiryDatePicker.first);
                fields.add(expiryDatePicker);

                {
                    if (entry.getPropertyNames().size() > 0) {
                        for (String pn : entry.getPropertyNames()) {
                            if (!pn.equalsIgnoreCase("username") && !pn.equalsIgnoreCase("password") && !pn.equalsIgnoreCase("url") && !pn.equalsIgnoreCase("title") && !pn.equalsIgnoreCase("notes")) {
                                final Triplet<View, TextInputEditText, ImageButton> additionalView = new FieldUtil().getAdditionalEditTextField2((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), pn, pn, entry.getProperty(pn));
                                Pair<View, TextInputEditText> additionalViewPair = new Pair<>();
                                additionalViewPair.first = additionalView.first;
                                additionalViewPair.second = additionalView.second;
                                fields.add(additionalViewPair);
                                additionalView.third.setOnClickListener(v1 -> {
                                    binding.editAdditionalFieldEntryScrollViewLinearLayout.removeView(additionalView.first);
                                    fieldsToRemove.add(additionalViewPair);
                                    fields.remove(additionalViewPair);
                                });
                                binding.editAdditionalFieldEntryScrollViewLinearLayout.addView(additionalView.first);
                            }
                        }
                    }
                }

                for (View dynamicView : viewsToAdd) {
                    /*@SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_left), Common.ANIMATION_TIME);
                    binding.editBasicFieldEntryScrollViewLinearLayout.setLayoutAnimation(lac);
                    binding.editBasicFieldEntryScrollViewLinearLayout.startLayoutAnimation();*/
                    binding.editBasicFieldEntryScrollViewLinearLayout.addView(dynamicView);
                }

                binding.editMoreFieldBtn.setOnClickListener(v -> {

                    final Triplet<View, TextInputEditText, ImageButton> additionalView = new FieldUtil().getAdditionalEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.enterFieldValue), "");
                    viewsToAdd.add(additionalView.first);
                    Pair<View, TextInputEditText> additionalViewPair = new Pair<>();
                    additionalViewPair.first = additionalView.first;
                    additionalViewPair.second = additionalView.second;
                    additionalView.third.setOnClickListener(v1 -> {
                        binding.editAdditionalFieldEntryScrollViewLinearLayout.removeView(additionalView.first);
                        fields.remove(additionalViewPair);
                    });
                    fields.add(additionalViewPair);
                    binding.editAdditionalFieldEntryScrollViewLinearLayout.addView(additionalView.first);
                    additionalView.first.requestFocus();
                });
                binding.editSaveEntry.setOnClickListener(v -> {
                    final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), EditEntryActivity.this);
                    ProgressDialogUtil.showSavingDialog(alertDialog);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            saveEntry(alertDialog, v);
                        }
                    }).start();
                });

                binding.home.setOnClickListener(v -> {
                    Common.group = Common.database.getRootGroup();
                    this.onBackPressed();
                });

                binding.generateNewPassword.setOnClickListener(v -> {
                    NewPasswordDialogUtil.show(getLayoutInflater(), v, (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
                });
                binding.lockBtn.setOnClickListener(v -> {
                    Common.database = null;
                    Common.group = null;
                    Common.entry = null;
                    Common.creds = null;
                    Common.kdbxFileUri = null;
                    Intent intent = new Intent(EditEntryActivity.this, LoadActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        }
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

    private void saveEntry(AlertDialog alertDialog, View v) {
        {
            boolean proceed = false;
            try {
                validate();
                proceed = true;
            } catch (KpCustomException e) {
                ProgressDialogUtil.dismissSavingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, e);
            }
            if (proceed) {
                Entry entry = Common.entry;
                ProgressDialogUtil.setSavingProgress(alertDialog, 20);
                for (Pair<View, TextInputEditText> fieldRemove : fieldsToRemove) {
                    entry.removeProperty(fieldRemove.second.getTag().toString());
                }
                for (Pair<View, TextInputEditText> field : fields) {
                    if (field.getSecond().getTag().toString().equalsIgnoreCase("title")) {
                        if (Util.isUsable(field.getSecond().getText().toString())) {
                            entry.setTitle(field.second.getText().toString());
                        }
                    } else if (field.getSecond().getTag().toString().equalsIgnoreCase("user name")) {
                        if (Util.isUsable(field.getSecond().getText().toString())) {
                            entry.setUsername(field.second.getText().toString());
                        }
                    } else if (field.getSecond().getTag().toString().equalsIgnoreCase("password")) {
                        if (Util.isUsable(field.getSecond().getText().toString())) {
                            entry.setPassword(field.second.getText().toString());
                        }
                    } else if (field.getSecond().getTag().toString().equalsIgnoreCase("url")) {
                        if (Util.isUsable(field.getSecond().getText().toString())) {
                            entry.setUrl(field.second.getText().toString());
                        }
                    } else if (field.getSecond().getTag().toString().equalsIgnoreCase("notes")) {
                        if (Util.isUsable(field.getSecond().getText().toString())) {
                            entry.setNotes(field.second.getText().toString());
                        }
                    } else if (field.getSecond().getTag().toString().equalsIgnoreCase("Expiry Date")) {
                        entry.setExpiryTime(Util.convertStringToDate(field.second.getText().toString()));
                    } else {
                        entry.setProperty(field.second.getTag().toString(), field.second.getText().toString());
                    }
                }

                boolean isOk = checkAndGetPermission(v, EditEntryActivity.this);

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
                        Intent intent = new Intent(EditEntryActivity.this, ListActivity.class);
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
        }
    }

    private void validate() throws KpCustomException {

        /*if (binding.editEntryTitleName.getText() == null || binding.editEntryTitleName.getText().toString() == null || binding.editEntryTitleName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryTitleEmptyErrorMsg);
        }
        if (binding.editEntryUserName.getText() == null || binding.editEntryUserName.getText().toString() == null || binding.editEntryUserName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryUsernameEmptyErrorMsg);
        }*/
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
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
            }
        } else {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
            }
        }
        return isOK;
    }
}