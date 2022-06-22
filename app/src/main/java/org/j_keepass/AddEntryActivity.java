package org.j_keepass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.j_keepass.databinding.ActivityAddEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;

public class AddEntryActivity extends AppCompatActivity {

    private ActivityAddEntryBinding binding;
    private static final int MANAGE_DOCUMENTS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(AddEntryActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {

            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), 0.5f); //0.5f == time between appearance of listview items.
            binding.addEntryScrollView.setLayoutAnimation(lac);
            binding.addEntryScrollView.startLayoutAnimation();

            {

                binding.newEntryTitleName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.newEntryTitleName.getText() != null) {
                            binding.newEntryTitleNameHeader.setText(binding.newEntryTitleName.getText().toString());
                        }
                    }
                });

                binding.newEntryUserNameCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.newEntryUserName.getText() != null) {
                        ClipData clip = ClipData.newPlainText("username", binding.newEntryUserName.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.newEntryPasswordCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.newEntryPassword.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.newEntryPassword.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.newEntryUrlCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.newEntryUrl.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.newEntryUrl.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.newEntryNotesCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.newEntryNotes.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.newEntryNotes.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });
                binding.saveNewEntry.setOnClickListener(v -> {
                    saveNewEntry(v);
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

    private void saveNewEntry(View v) {
        final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), AddEntryActivity.this);
        ProgressDialogUtil.showSavingDialog(alertDialog);

        new Thread(() -> {
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
                    Entry entry = Common.database.newEntry();
                    if (binding.newEntryTitleName.getText() != null) {
                        entry.setTitle(binding.newEntryTitleName.getText().toString());
                    }
                    if (binding.newEntryUserName.getText() != null) {
                        entry.setUsername(binding.newEntryUserName.getText().toString());
                    }
                    if (binding.newEntryPassword.getText() != null) {
                        entry.setPassword(binding.newEntryPassword.getText().toString());
                    }
                    if (binding.newEntryUrl.getText() != null) {
                        entry.setUrl(binding.newEntryUrl.getText().toString());
                    }
                    if (binding.newEntryNotes.getText() != null) {
                        entry.setNotes(binding.newEntryNotes.getText().toString());
                    }

                    Group group = Common.group;
                    group.addEntry(entry);
                    Common.group = group;

                    if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddEntryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
                            Intent intent = new Intent(AddEntryActivity.this, ListActivity.class);
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
        }).start();
    }

    private void validate() throws KpCustomException {

        if (binding.newEntryTitleName.getText() == null || binding.newEntryTitleName.getText().toString() == null || binding.newEntryTitleName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryTitleEmptyErrorMsg);
        }
        if (binding.newEntryUserName.getText() == null || binding.newEntryUserName.getText().toString() == null || binding.newEntryUserName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryUsernameEmptyErrorMsg);
        }
        if( !Common.isCodecAvailable)
        {
            throw new KpCustomException(R.string.devInProgress);
        }
    }
}