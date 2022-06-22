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

import org.j_keepass.databinding.ActivityEditEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.OutputStream;

public class EditEntryActivity extends AppCompatActivity {

    private ActivityEditEntryBinding binding;
    private static final int MANAGE_DOCUMENTS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), 0.5f); //0.5f == time between appearance of listview items.
            binding.editEntryScrollView.setLayoutAnimation(lac);
            binding.editEntryScrollView.startLayoutAnimation();

            if (entry != null) {
                binding.editEntryTitleName.setText(entry.getTitle());
                binding.editEntryTitleNameHeader.setText(entry.getTitle());
                binding.editEntryUserName.setText(entry.getUsername());
                binding.editEntryPassword.setText(entry.getPassword());
                binding.editEntryUrl.setText(entry.getUrl());
                binding.editEntryNotes.setText(entry.getNotes());

                binding.editEntryTitleName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (binding.editEntryTitleName.getText() != null) {
                            binding.editEntryTitleNameHeader.setText(binding.editEntryTitleName.getText().toString());
                        }
                    }
                });

                binding.editEntryUserNameCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.editEntryUserName.getText() != null) {
                        ClipData clip = ClipData.newPlainText("username", binding.editEntryUserName.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.editEntryPasswordCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.editEntryPassword.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.editEntryPassword.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.editEntryUrlCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.editEntryUrl.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.editEntryUrl.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.editEntryNotesCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.editEntryNotes.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.editEntryNotes.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });
                binding.editSaveEntry.setOnClickListener(v -> {
                    saveEntry(v);
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

    private void saveEntry(View v) {
        final AlertDialog alertDialog = ProgressDialogUtil.getSaving(getLayoutInflater(), EditEntryActivity.this);
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
                    Entry entry = Common.entry;
                    if (binding.editEntryTitleName.getText() != null) {
                        entry.setTitle(binding.editEntryTitleName.getText().toString());
                    }
                    if (binding.editEntryUserName.getText() != null) {
                        entry.setUsername(binding.editEntryUserName.getText().toString());
                    }
                    if (binding.editEntryPassword.getText() != null) {
                        entry.setPassword(binding.editEntryPassword.getText().toString());
                    }
                    if (binding.editEntryUrl.getText() != null) {
                        entry.setUrl(binding.editEntryUrl.getText().toString());
                    }
                    if (binding.editEntryNotes.getText() != null) {
                        entry.setNotes(binding.editEntryNotes.getText().toString());
                    }

                    if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditEntryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        }).start();
    }

    private void validate() throws KpCustomException {

        if (binding.editEntryTitleName.getText() == null || binding.editEntryTitleName.getText().toString() == null || binding.editEntryTitleName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryTitleEmptyErrorMsg);
        }
        if (binding.editEntryUserName.getText() == null || binding.editEntryUserName.getText().toString() == null || binding.editEntryUserName.getText().toString().length() <= 0) {
            throw new KpCustomException(R.string.entryUsernameEmptyErrorMsg);
        }
        if( !Common.isCodecAvailable)
        {
            throw new KpCustomException(R.string.devInProgress);
        }
    }
}