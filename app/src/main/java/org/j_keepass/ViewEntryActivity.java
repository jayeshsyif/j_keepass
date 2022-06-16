package org.j_keepass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.j_keepass.databinding.ActivityViewEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Entry;

public class ViewEntryActivity extends AppCompatActivity {

    private ActivityViewEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(ViewEntryActivity.this, LoadActivity.class);
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
                binding.entryTitleName.setText(entry.getTitle());
                binding.entryUserName.setText(entry.getUsername());
                binding.entryPassword.setText(entry.getPassword());
                binding.entryUrl.setText(entry.getUrl());
                binding.entryNotes.setText(entry.getNotes());

                binding.userNameCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.entryUserName.getText() != null) {
                        ClipData clip = ClipData.newPlainText("username", binding.entryUserName.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.entryPasswordCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.entryPassword.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.entryPassword.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.entryUrlCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.entryUrl.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.entryUrl.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });

                binding.entryNotesCopy.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    if (binding.entryNotes.getText() != null) {
                        ClipData clip = ClipData.newPlainText("password", binding.entryNotes.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.copiedToClipboard);
                    }
                });
            }
        }
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
}