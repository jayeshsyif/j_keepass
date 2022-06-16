package org.j_keepass;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import org.j_keepass.databinding.AddNewGroupLayoutBinding;
import org.j_keepass.util.Common;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class AddGroupActivity extends AppCompatActivity {

    private AddNewGroupLayoutBinding binding;
    private static final int MANAGE_DOCUMENTS = 200;
    public static final int PICK_FILE_WRITE_RESULT_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddNewGroupLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Common.database == null) {
            Intent intent = new Intent(AddGroupActivity.this, LoadActivity.class);
            startActivity(intent);
        } else {

            binding.addGroupName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    binding.addGroupTitleName.setText(binding.addGroupName.getText().toString());
                }
            });
            binding.saveGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = Util.getSaving(getLayoutInflater(), AddGroupActivity.this);
                    Util.showSavingDialog(alertDialog);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String groupName = binding.addGroupName.getText().toString();
                            if (groupName == null || groupName.length() <= 0 || groupName.isEmpty()) {
                                Util.dismissSavingDialog(alertDialog);
                                Snackbar.make(v, R.string.groupNameEmptyErrorMsg, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Group group = Common.group;
                                Group newGroup = Common.database.newGroup();
                                newGroup.setName(groupName);
                                newGroup.setParent(group);
                                group.addGroup(newGroup);
                                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(AddGroupActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MANAGE_DOCUMENTS);
                                }

                                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    Util.setSavingProgress(alertDialog, 30);
                                    OutputStream fileOutputStream = null;
                                    try {
                                        getContentResolver().takePersistableUriPermission(Common.kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                        Util.setSavingProgress(alertDialog, 40);
                                        fileOutputStream = getContentResolver().openOutputStream(Common.kdbxFileUri, "wt");
                                        Util.setSavingProgress(alertDialog, 50);
                                        Common.database.save(Common.creds, fileOutputStream);
                                        Util.setSavingProgress(alertDialog, 100);
                                        Intent intent = new Intent(AddGroupActivity.this, ListActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("click", "group");
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        finish();
                                    } catch (Exception e) {
                                        Util.dismissSavingDialog(alertDialog);
                                        Snackbar.make(v, "" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                        binding.addGroupNotes.setText(e.getMessage());
                                    } finally {
                                        if (fileOutputStream != null) {
                                            try {
                                                fileOutputStream.close();
                                            } catch (IOException e) {
                                            }
                                        }
                                    }
                                } else {
                                    Util.dismissSavingDialog(alertDialog);
                                    Snackbar.make(v, R.string.permissionNotGranted, Snackbar.LENGTH_LONG).show();
                                }

                            }
                        }
                    }).start();
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FILE_WRITE_RESULT_CODE:
        }
    }
}