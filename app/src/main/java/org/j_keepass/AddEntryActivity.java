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
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityAddEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.NewPasswordDialogUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ThemeSettingDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddEntryActivity extends AppCompatActivity {

    private ActivityAddEntryBinding binding;
    private ArrayList<Pair<View, TextInputEditText>> fields = new ArrayList<Pair<View, TextInputEditText>>();

    private static final int READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeSettingDialogUtil.onActivityCreateSetTheme(this, false);
        binding = ActivityAddEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(AddEntryActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {
            binding.addBasicFieldEntryScrollViewLinearLayout.removeAllViews();
            ArrayList<View> viewsToAdd = new ArrayList<View>();

            final Pair<View, TextInputEditText> titleView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.entryTitle), "");
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
                        binding.newEntryTitleNameHeader.setText(titleView.second.getText().toString());
                    }
                }
            });
            viewsToAdd.add(titleView.first);
            fields.add(titleView);

            final Pair<View, TextInputEditText> userNameView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.userName), "");
            viewsToAdd.add(userNameView.first);
            fields.add(userNameView);

            final Pair<View, TextInputEditText> passwordView = new FieldUtil().getEditPasswordField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.password), "");
            viewsToAdd.add(passwordView.first);
            fields.add(passwordView);

            final Pair<View, TextInputEditText> urlView = new FieldUtil().getEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.url), "");
            viewsToAdd.add(urlView.first);
            fields.add(urlView);

            final Pair<View, TextInputEditText> notesView = new FieldUtil().getMultiEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.notes), "");
            viewsToAdd.add(notesView.first);
            fields.add(notesView);

            Date currentDate = Calendar.getInstance().getTime();
            final Pair<View, TextInputEditText> expiryDatePicker = new FieldUtil().getDatePickerTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.expiryDate), currentDate);
            viewsToAdd.add(expiryDatePicker.first);
            fields.add(expiryDatePicker);

            new Thread(() -> {
                for (View dynamicView : viewsToAdd) {
                    Util.sleepFor100Sec();
                    runOnUiThread(() -> {
            /*    @SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_left), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
                binding.addBasicFieldEntryScrollViewLinearLayout.setLayoutAnimation(lac);
                binding.addBasicFieldEntryScrollViewLinearLayout.startLayoutAnimation();*/
                        binding.addBasicFieldEntryScrollViewLinearLayout.addView(dynamicView);
                    });
                }
            }).start();

            binding.addMoreFieldBtn.setOnClickListener(v -> {

                final Triplet<View, TextInputEditText, ImageButton> additionalView = new FieldUtil().getAdditionalEditTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), getString(R.string.enterFieldValue), "");
                viewsToAdd.add(additionalView.first);
                Pair<View, TextInputEditText> additionalViewPair = new Pair<>();
                additionalViewPair.first = additionalView.first;
                additionalViewPair.second = additionalView.second;
                additionalView.third.setOnClickListener(v1 -> {
                    binding.addAdditionalFieldEntryScrollViewLinearLayout.removeView(additionalView.first);
                    fields.remove(additionalViewPair);
                });
                fields.add(additionalViewPair);
                binding.addAdditionalFieldEntryScrollViewLinearLayout.addView(additionalView.first);
                additionalView.first.requestFocus();
            });
            binding.saveNewEntry.setOnClickListener(v -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), AddEntryActivity.this);
                ProgressDialogUtil.showSavingDialog(alertDialog);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveNewEntry(alertDialog, v);
                    }
                }).start();
            });
            binding.home.setOnClickListener(v -> {
                Common.group = Common.database.getRootGroup();
                this.onBackPressed();
            });

            binding.generateNewPassword.setOnClickListener(v -> {
                NewPasswordDialogUtil.show(getLayoutInflater(), v, (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), binding.getRoot().findViewById(R.id.saveNewEntry));
            });
            binding.lockBtn.setOnClickListener(v -> {
                Common.database = null;
                Common.group = null;
                Common.entry = null;
                Common.creds = null;
                Common.kdbxFileUri = null;
                Intent intent = new Intent(AddEntryActivity.this, LoadActivity.class);
                startActivity(intent);
                finish();
            });
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

    private void saveNewEntry(AlertDialog alertDialog, View v) {
        boolean proceed = false;
        try {
            validate();
            proceed = true;
        } catch (KpCustomException e) {
            ProgressDialogUtil.dismissSavingDialog(alertDialog);
            ToastUtil.showToast(getLayoutInflater(), v, e, binding.getRoot().findViewById(R.id.saveNewEntry));
        }

        if (proceed) {
            Entry entry = Common.database.newEntry();

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
            Group group = Common.group;
            group.addEntry(entry);
            Common.group = group;

            boolean isOk = checkAndGetPermission(v, AddEntryActivity.this);

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
                    Intent intent = new Intent(AddEntryActivity.this, ListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("click", "group");
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                } catch (NoSuchMethodError e) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.saveNewEntry));
                } catch (Exception e) {
                    ProgressDialogUtil.dismissSavingDialog(alertDialog);
                    ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.saveNewEntry));
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
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.saveNewEntry));
            }
        }
    }

    private void validate() throws KpCustomException {

        if (binding.newEntryTitleNameHeader.getText() == null || binding.newEntryTitleNameHeader.getText().toString() == null || binding.newEntryTitleNameHeader.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryTitleEmptyErrorMsg);
        }
        /*if (binding.newEntryUserName.getText() == null || binding.newEntryUserName.getText().toString() == null || binding.newEntryUserName.getText().toString().length() <= 0) {
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
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.saveNewEntry));
            }
        } else {
            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                isOK = true;
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.saveNewEntry));
            }
        }
        return isOK;
    }
}