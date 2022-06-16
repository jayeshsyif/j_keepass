package org.j_keepass;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityLoadBinding;
import org.j_keepass.util.Common;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LoadActivity extends AppCompatActivity {
    public static final int PICK_FILE_OPEN_RESULT_CODE = 1;
    private AppBarConfiguration appBarConfiguration;
    private ActivityLoadBinding binding;
    private Uri kdbxFileUri = null;
    private static final int READ_EXTERNAL_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Common.database = null;

        MaterialButton openBtn = binding.openBtn;
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(LoadActivity.this, new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE);
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    chooseFile.setType("*/*");
                    chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(chooseFile, PICK_FILE_OPEN_RESULT_CODE);
                } else {
                    Snackbar.make(v, R.string.permissionNotGranted, Snackbar.LENGTH_LONG).show();
                }


            }
        });


        ImageButton okBtn = binding.okBtn;
        TextInputEditText kdbxPasswordET = binding.kdbxPassword;
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.processingFile, Snackbar.LENGTH_SHORT).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean proceed = true;
                        String kdbxPassword = null;
                        Database<?, ?, ?, ?> database = null;
                        if (kdbxPasswordET.getText() != null) {
                            kdbxPassword = kdbxPasswordET.getText().toString();
                            if (kdbxPassword == null || kdbxPassword.length() <= 0) {
                                Snackbar.make(v, R.string.emptyPasswordError, Snackbar.LENGTH_LONG).show();
                                proceed = false;
                            }
                        } else {
                            Snackbar.make(v, R.string.emptyPasswordError, Snackbar.LENGTH_LONG).show();
                            proceed = false;
                        }

                        if (proceed) {
                            if (kdbxFileUri == null) {
                                Snackbar.make(v, R.string.emptyFileError, Snackbar.LENGTH_LONG).show();
                                proceed = false;
                            }
                        }
                        if (proceed) {
                            KdbxCreds creds = new KdbxCreds(kdbxPassword.getBytes());
                            Common.creds = creds;
                            InputStream inputStream = null;
                            try {
                                inputStream = getContentResolver().openInputStream(kdbxFileUri);
                            } catch (FileNotFoundException e) {
                                proceed = false;
                                Snackbar.make(v, R.string.invalidFileError + " " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                            if (inputStream != null) {
                                try {
                                    database = SimpleDatabase.load(creds, inputStream);
                                } catch (Exception e) {
                                    proceed = false;
                                    Snackbar.make(v, R.string.invalidFileError + " " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                                if (database == null) {
                                    Snackbar.make(v, R.string.noDBError, Snackbar.LENGTH_LONG).show();
                                    proceed = false;
                                }
                            }

                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Exception e) {
                                }
                            }
                        }

                        if (proceed) {
                            if (database != null) {
                                try {
                                    Common.database = database;
                                    Common.kdbxFileUri = kdbxFileUri;
                                    Intent intent = new Intent(LoadActivity.this, ListActivity.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Snackbar.make(v, R.string.unableToNavigateError, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                }).start();
            }
        });

        MaterialButton createBtn = binding.createBtn;
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, R.string.devInProgress, Snackbar.LENGTH_SHORT).show();
            }
        });
        binding.floatInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(LoadActivity.this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
                dialog.setContentView(R.layout.info_layout);
                FloatingActionButton closeInfoBtn = dialog.findViewById(R.id.floatCloseInfoBtn);
                closeInfoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FILE_OPEN_RESULT_CODE:
                if (resultCode == -1) {
                    kdbxFileUri = data.getData();
                    String fileName = "";
                    try {

                        getContentResolver().takePersistableUriPermission(kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Cursor returnCursor =
                                getContentResolver().query(kdbxFileUri, null, null, null, null);
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        returnCursor.moveToFirst();
                        fileName = returnCursor.getString(nameIndex);

                    } catch (Exception e) {
                        fileName = e.getMessage();
                    }

                    binding.kdbxFilePath.setText(fileName);
                }

                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Common.database = null;
    }
}