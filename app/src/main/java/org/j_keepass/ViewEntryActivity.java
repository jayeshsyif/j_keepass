package org.j_keepass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;

import org.j_keepass.databinding.ActivityViewEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Entry;

public class ViewEntryActivity extends AppCompatActivity {

    private ActivityViewEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
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
                LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left), 0.5f); //0.5f == time between appearance of listview items.
                binding.viewEntryScrollViewLinearLayout.setLayoutAnimation(lac);
                binding.viewEntryScrollViewLinearLayout.startLayoutAnimation();

                binding.entryTitleName.setText(entry.getTitle());
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (Util.isUsable(entry.getUsername())) {
                    final View userNameView = FieldUtil.getTextFieldWithCopy(inflater, getString(R.string.userName), entry.getUsername(), clipboard, getString(R.string.copiedToClipboard));
                    binding.viewEntryScrollViewLinearLayout.addView(userNameView);
                }

                if (Util.isUsable(entry.getPassword())) {
                    final View passwordView = FieldUtil.getPasswordFieldWithCopy(inflater, getString(R.string.password), entry.getPassword(), clipboard, getString(R.string.copiedToClipboard));
                    binding.viewEntryScrollViewLinearLayout.addView(passwordView);
                    binding.entryPasswordCopyFloatBtn.setOnClickListener(v -> {
                        passwordView.findViewById(R.id.fieldCopy).performClick();
                    });
                }

                if (Util.isUsable(entry.getUrl())) {
                    final View urlView = FieldUtil.getTextFieldWithCopy(inflater, getString(R.string.url), entry.getUrl(), clipboard, getString(R.string.copiedToClipboard));
                    binding.viewEntryScrollViewLinearLayout.addView(urlView);
                }
                if (Util.isUsable(entry.getNotes())) {
                    final View notesView = FieldUtil.getMultiLineTextFieldWithCopy(inflater, getString(R.string.notes), entry.getNotes(), clipboard, getString(R.string.copiedToClipboard));
                    binding.viewEntryScrollViewLinearLayout.addView(notesView);
                }


            }
        }
        binding.backFloatBtn.setOnClickListener(v -> {
            this.onBackPressed();
        });
        binding.searchFloatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            finish();
        });
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            binding.viewEntryScrollView.setVisibility(View.GONE);
        }
        if (hasFocus) {
            binding.viewEntryScrollView.setVisibility(View.VISIBLE);
        }
    }
}