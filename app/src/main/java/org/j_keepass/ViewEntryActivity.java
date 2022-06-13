package org.j_keepass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.databinding.ActivityLoadBinding;
import org.j_keepass.databinding.ActivityViewEntryBinding;
import org.j_keepass.util.Common;
import org.linguafranca.pwdb.Database;
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
            Database<?, ?, ?, ?> database = Common.database;
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

                binding.userNameCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("username", binding.entryUserName.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(v, R.string.copiedToClipboard, Snackbar.LENGTH_SHORT).show();
                    }
                });

                binding.entryPasswordCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("password", binding.entryPassword.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(v, R.string.copiedToClipboard, Snackbar.LENGTH_SHORT).show();
                    }
                });

                binding.entryUrlCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("password", binding.entryUrl.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(v, R.string.copiedToClipboard, Snackbar.LENGTH_SHORT).show();
                    }
                });

                binding.entryNotesCopy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager)
                                getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("password", binding.entryNotes.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Snackbar.make(v, R.string.copiedToClipboard, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}