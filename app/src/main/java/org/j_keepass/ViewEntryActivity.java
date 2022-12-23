package org.j_keepass;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;

import org.j_keepass.databinding.ActivityViewEntryBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.FieldUtil;
import org.j_keepass.util.NewPasswordDialogUtil;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Entry;

import java.util.ArrayList;

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
            final Entry<?, ?, ?, ?> finalEntry = entry;
            if (entry != null) {
                binding.entryTitleName.setText(entry.getTitle());

                binding.viewEntryScrollViewLinearLayout.removeAllViews();
                ArrayList<View> viewsToAdd = new ArrayList<View>();
                if (Util.isUsable(finalEntry.getUsername())) {
                    final View userNameView = new FieldUtil().getTextFieldWithCopy((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getString(R.string.userName), finalEntry.getUsername(), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), getString(R.string.copiedToClipboard));
                    viewsToAdd.add(userNameView);
                }

                if (Util.isUsable(finalEntry.getPassword())) {
                    final View passwordView = new FieldUtil().getPasswordFieldWithCopy((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getString(R.string.password), finalEntry.getPassword(), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), getString(R.string.copiedToClipboard));
                    viewsToAdd.add(passwordView);
                    binding.entryPasswordCopyFloatBtn.setOnClickListener(v -> {
                        passwordView.findViewById(R.id.fieldCopy).performClick();
                    });
                }

                if (Util.isUsable(finalEntry.getUrl())) {
                    final View urlView = new FieldUtil().getTextFieldWithCopy((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getString(R.string.url), finalEntry.getUrl(), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), getString(R.string.copiedToClipboard));
                    viewsToAdd.add(urlView);
                }
                if (Util.isUsable(finalEntry.getNotes())) {
                    final View notesView = new FieldUtil().getMultiLineTextFieldWithCopy((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getString(R.string.notes), finalEntry.getNotes(), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), getString(R.string.copiedToClipboard));
                    viewsToAdd.add(notesView);
                }
                {
                    if (entry.getPropertyNames().size() > 0) {
                        for (String pn : entry.getPropertyNames()) {
                            if (!pn.equalsIgnoreCase("username") && !pn.equalsIgnoreCase("password")
                                    && !pn.equalsIgnoreCase("url") && !pn.equalsIgnoreCase("title") && !pn.equalsIgnoreCase("notes")) {
                                final View pnView = new FieldUtil().getTextFieldWithCopy((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                                        pn, entry.getProperty(pn), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), getString(R.string.copiedToClipboard));
                                viewsToAdd.add(pnView);
                            }
                        }
                    }
                }
                {
                    final View creationView = new FieldUtil().getTextField((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getString(R.string.creationDate), Util.convertDateToString(finalEntry.getCreationTime()));
                    viewsToAdd.add(creationView);
                }
                for (View dynamicView : viewsToAdd) {
                    /*@SuppressLint("ResourceType") LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME); //0.5f == time between appearance of listview items.
                    binding.viewEntryScrollViewLinearLayout.setLayoutAnimation(lac);
                    binding.viewEntryScrollViewLinearLayout.startLayoutAnimation();*/
                    binding.viewEntryScrollViewLinearLayout.addView(dynamicView);
                }
            }
        }

        binding.home.setOnClickListener( v -> {
            Common.group = Common.database.getRootGroup();
            this.onBackPressed();
        });

        binding.generateNewPassword.setOnClickListener( v -> {
            AlertDialog d = NewPasswordDialogUtil.getDialog(getLayoutInflater(), binding.getRoot().getContext(), (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE));
            NewPasswordDialogUtil.showDialog(d);
        });

        binding.edit.setOnClickListener( v -> {
            Intent intent = new Intent(this, EditEntryActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putString("click", "entry");
            intent.putExtras(bundle1);
            startActivity(intent);
            finish();
        });

        binding.lockBtn.setOnClickListener(v -> {
            Common.database = null;
            Common.group = null;
            Common.entry = null;
            Common.creds = null;
            Common.kdbxFileUri = null;
            Intent intent = new Intent(ViewEntryActivity.this, LoadActivity.class);
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