package org.j_keepass;

import android.Manifest;
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
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.Icon;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;

import java.io.FileOutputStream;
import java.io.IOException;


public class AddGroupActivity extends AppCompatActivity {

    private AddNewGroupLayoutBinding binding;
    private static final int WRITE_EXTERNAL_STORAGE = 200;
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

                    String groupName = binding.addGroupName.getText().toString();
                    if (groupName == null || groupName.length() <= 0 || groupName.isEmpty()) {
                        Snackbar.make(v, R.string.groupNameEmptyErrorMsg, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Group group = Common.group;
                        Group newGroup = Common.database.newGroup();
                        newGroup.setName(groupName);
                        newGroup.setParent(group);
                        group.addGroup(newGroup);
                        if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(AddGroupActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    WRITE_EXTERNAL_STORAGE);
                        }

                        if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                            ParcelFileDescriptor pfd = null;
                            FileOutputStream fileOutputStream = null;
                            try {
                                pfd = getContentResolver().
                                        openFileDescriptor(Common.kdbxFileUri, "w");
                                fileOutputStream =
                                        new FileOutputStream(pfd.getFileDescriptor());

                                Common.database.save(Common.creds, fileOutputStream);

                                Intent intent = new Intent(AddGroupActivity.this, ListActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("click", "group");
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e("KP ", "KP ", e);
                                Snackbar.make(v, "" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            } finally {
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                    } catch (IOException e) {
                                    }
                                }

                                if (pfd != null) {
                                    try {
                                        pfd.close();
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        } else {
                            Snackbar.make(v, R.string.permissionNotGranted, Snackbar.LENGTH_LONG).show();
                        }

                    }
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