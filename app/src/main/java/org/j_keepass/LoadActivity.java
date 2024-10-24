package org.j_keepass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.io.ByteStreams;

import org.j_keepass.databinding.ActivityLoadBinding;
import org.j_keepass.util.BannerDialogUtil;
import org.j_keepass.util.BottomMenuUtil;
import org.j_keepass.util.Common;
import org.j_keepass.util.ConfirmDialogUtil;
import org.j_keepass.util.DatabaseCreateDialogUtil;
import org.j_keepass.util.InfoDialogUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.NewPasswordDialogUtil;
import org.j_keepass.util.Pair;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.Quadruple;
import org.j_keepass.util.ThemeSettingDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class LoadActivity extends AppCompatActivity {
    public static final int PICK_FILE_OPEN_RESULT_CODE = 1;
    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;
    private ActivityLoadBinding binding;
    private Uri kdbxFileUri = null;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private static boolean IS_READ_EXTERNAL_STORAGE_RECEIVED = false;
    private static final int ALARM = 101;
    private static boolean IS_ALARM_PERMISSION_RECEIVED = false;
    private int defaultThemeCode = Configuration.UI_MODE_NIGHT_NO;
    private String dirPath = null;
    private String subFilesDirPath = null;
    boolean isFileAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeSettingDialogUtil.onActivityCreateSetTheme(this, true);
        binding = ActivityLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setThemeButton();

        checkCodecAvailable();


        if (getIntent() != null && getIntent().getData() != null) {
            kdbxFileUri = getIntent().getData();
            isFileAvailable = true;
        } else {
            isFileAvailable = false;
        }

        Common.database = null;
        if (isFileAvailable) {
            Triplet<AlertDialog, MaterialButton, MaterialButton> importConfirmDialog = ConfirmDialogUtil.getImportConfirmDialog(getLayoutInflater(), this);
            importConfirmDialog.third.setOnClickListener(v -> {
                importConfirmDialog.first.dismiss();
                loadFile();
                showOpenDialog(binding.kdbxFileName.getText().toString(), v);
            });
            importConfirmDialog.second.setOnClickListener(v -> {
                importConfirmDialog.first.dismiss();
                importFile();
                showOpenDialog(binding.kdbxFileName.getText().toString(), v);
            });
            ConfirmDialogUtil.showDialog(importConfirmDialog.first);
        }
        dirPath = getFilesDir().getPath() + File.separator + "org.j_keepass";
        subFilesDirPath = getFilesDir().getPath() + File.separator + "org.j_keepass" + File.separator + "kdbxfiles";
        setEvents();

        if (isFileAvailable) {
            binding.openImportLayout.setVisibility(View.GONE);
            binding.justDatabaseText.setVisibility(View.GONE);
            binding.justImportCreateTextView.setVisibility(View.GONE);
            //ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterPassword);
        } else {
            binding.openImportLayout.setVisibility(View.GONE);
            binding.justDatabaseText.setVisibility(View.VISIBLE);
            binding.justImportCreateTextView.setVisibility(View.VISIBLE);
        }
        binding.kdbxFileName.setVisibility(View.GONE);
        binding.kdbxFileGotPasswordLayout.setVisibility(View.GONE);

        loadAfterDialog();
    }

    private void loadAfterDialog() {
        new Thread(() -> {
            showWelcomeMessage(binding.getRoot());
            if (!isFileAvailable) {
                fetchAndShowFiles();
            }
            startAlarmBroadcastReceiver(binding.getRoot().getContext());
        }).start();
    }

    private void setEvents() {
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;

        kdbxPasswordET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (binding.getRoot().getContext() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        binding.floatGenerateNewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewPasswordDialogUtil.show(getLayoutInflater(), v, (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
            }
        });
        binding.importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isOk = checkAndGetPermission(v, LoadActivity.this);

                if (isOk) {
                    Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    chooseFile.setType("*/*");
                    chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                    startActivityForResult(chooseFile, PICK_FILE_OPEN_RESULT_CODE);
                } else {
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            }
        });
        ImageButton createBtn = binding.createBtn;
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isOK = checkAndGetPermission(v, LoadActivity.this);

                if (isOK) {
                    Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> confirmDialog = DatabaseCreateDialogUtil.getConfirmDialog(getLayoutInflater(), binding.getRoot().getContext());

                    confirmDialog.second.setOnClickListener(v1 -> {
                        if (!Common.isCodecAvailable) {
                            confirmDialog.first.dismiss();
                            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.devInProgress, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                        } else if (confirmDialog.third.getText() == null || confirmDialog.third.getText().toString().length() <= 0) {
                            confirmDialog.first.dismiss();
                            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterDatabaseName, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                        } else if (confirmDialog.fourth.getText() == null || confirmDialog.fourth.getText().toString().length() <= 0) {
                            confirmDialog.first.dismiss();
                            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterPassword, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                        } else {
                            String dbName = confirmDialog.third.getText().toString();
                            confirmDialog.first.dismiss();
                            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.creating, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                            if (!dbName.endsWith("kdbx")) {
                                dbName = dbName + ".kdbx";
                            }
                            File fromTo = new File(subFilesDirPath + File.separator + dbName);
                            if (fromTo.exists()) {
                                fromTo.delete();
                            } else {
                                new Thread(() -> {
                                    try {
                                        fromTo.createNewFile();
                                        kdbxFileUri = Uri.fromFile(fromTo);
                                        KdbxCreds creds = new KdbxCreds(confirmDialog.fourth.getText().toString().getBytes());
                                        Database<?, ?, ?, ?> database = getDummyDatabase();
                                        OutputStream fileOutputStream = getContentResolver().openOutputStream(kdbxFileUri, "wt");
                                        database.save(creds, fileOutputStream);
                                        if (v1 != null) {
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                                        }
                                        fetchAndShowFiles();
                                        kdbxFileUri = null;
                                    } catch (Exception e) {
                                        ToastUtil.showToast(getLayoutInflater(), v1, e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                                    }
                                }).start();
                            }
                        }

                    });
                    confirmDialog.first.show();
                }
            }
        });
        binding.floatInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog();
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
                    Log.i("JKeepass", "Flags: " + data.getFlags());
                    loadFile();
                    copyFile();
                    new Thread(() -> {
                        fetchAndShowFiles();
                    }).start();
                }
                break;
            case PICK_FOLDER_OPEN_RESULT_CODE:
                if (resultCode == -1) {
                   /* kdbxFileUri = data.getData();
                    ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterPassword);
                    loadFile();*/
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

    private void loadFile() {
        if (kdbxFileUri != null) {
            String fileName = "";
            try {
                getContentResolver().takePersistableUriPermission(kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                this.grantUriPermission(this.getPackageName(), kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) {
                if (!isFileAvailable) {
                    ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            }
            try {
                Cursor returnCursor = getContentResolver().query(kdbxFileUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                fileName = returnCursor.getString(nameIndex);
                binding.kdbxFileName.setText(fileName);
            } catch (Exception e) {
                fileName = e.getMessage();
            }
        }
    }

    private void importFile() {
        if (kdbxFileUri != null) {
            loadFile();
            copyFile();
        }
    }

    private void process(AlertDialog alertDialog, View v) {
        boolean proceed = true;
        String kdbxPassword = null;
        Database<?, ?, ?, ?> database = null;
        ProgressDialogUtil.setLoadingProgress(alertDialog, 20);
        try {
            Validate();
        } catch (KpCustomException e) {
            ProgressDialogUtil.dismissLoadingDialog(alertDialog);
            ToastUtil.showToast(getLayoutInflater(), v, e, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
            proceed = false;
        }
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;
        ProgressDialogUtil.setLoadingProgress(alertDialog, 30);
        if (proceed) {
            if (kdbxFileUri == null) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, R.string.emptyFileError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                proceed = false;
            } else {
                /*try {
                    getContentResolver().takePersistableUriPermission(kdbxFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception e) {
                    //proceed = false;
                    ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError);
                }*/
            }
        }
        ProgressDialogUtil.setLoadingProgress(alertDialog, 40);
        kdbxPassword = kdbxPasswordET.getText().toString();
        if (proceed) {
            KdbxCreds creds = new KdbxCreds(kdbxPassword.getBytes());
            Common.creds = creds;
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(kdbxFileUri);
            } catch (FileNotFoundException e) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                proceed = false;
                ToastUtil.showToast(getLayoutInflater(), v, R.string.invalidFileError + " " + e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
            }
            ProgressDialogUtil.setLoadingProgress(alertDialog, 60);
            if (inputStream != null) {
                try {
                    database = SimpleDatabase.load(creds, inputStream);
                    ProgressDialogUtil.setLoadingProgress(alertDialog, 90);
                } catch (NoClassDefFoundError e) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    proceed = false;
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.devInProgress, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                } catch (Exception e) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    proceed = false;
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.invalidFileError + " " + e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
                if (proceed && database == null) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.noDBError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
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
                    ProgressDialogUtil.setLoadingProgress(alertDialog, 100);
                    Common.database = database;
                    Common.kdbxFileUri = kdbxFileUri;
                    Common.group = database.getRootGroup();
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    Intent intent = new Intent(LoadActivity.this, ListActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.unableToNavigateError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            }
        }

        if (!proceed) {
            ProgressDialogUtil.dismissLoadingDialog(alertDialog);
        }
    }

    private void createFile(AlertDialog alertDialog, View v) {
        boolean proceed = true;
        ProgressDialogUtil.setLoadingProgress(alertDialog, 20);
        try {
            Validate();
            ValidateCodec();
        } catch (KpCustomException e) {
            ProgressDialogUtil.dismissLoadingDialog(alertDialog);
            ToastUtil.showToast(getLayoutInflater(), v, e, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
            proceed = false;
        }
        if (proceed) {
            TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;
            String kdbxPassword = kdbxPasswordET.getText().toString();
            KdbxCreds creds = new KdbxCreds(kdbxPassword.getBytes());
            Common.creds = creds;
            ProgressDialogUtil.setLoadingProgress(alertDialog, 30);
            Database<?, ?, ?, ?> database = getDummyDatabase();

            OutputStream fileOutputStream = null;
            try {
                ProgressDialogUtil.setLoadingProgress(alertDialog, 40);
                fileOutputStream = getContentResolver().openOutputStream(kdbxFileUri, "wt");
                ProgressDialogUtil.setLoadingProgress(alertDialog, 50);
                database.save(creds, fileOutputStream);
                ProgressDialogUtil.setLoadingProgress(alertDialog, 90);
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
            } catch (NoSuchMethodError e) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
            } catch (Exception e) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                Log.e("KP", "KP error ", e);
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }
            if (proceed) {
                if (database != null) {
                    try {
                        ProgressDialogUtil.setLoadingProgress(alertDialog, 100);
                        Common.database = database;
                        Common.kdbxFileUri = kdbxFileUri;
                        ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                        Intent intent = new Intent(LoadActivity.this, ListActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.unableToNavigateError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    }
                }
            }
        }
    }

    private Database<?, ?, ?, ?> getDummyDatabase() {
        Database<?, ?, ?, ?> database = new SimpleDatabase();
        Group rootGroup = database.getRootGroup();
        rootGroup.setName("Root");
        Calendar currentCalender = Calendar.getInstance();
        int currentCalenderMonth = currentCalender.get(Calendar.MONTH);
        if (currentCalenderMonth == 11) {
            currentCalenderMonth = 0;
        }
        currentCalender.set(Calendar.MONTH, currentCalenderMonth + 1);
        {
            Group g = database.newGroup("Bank");
            Entry e1 = database.newEntry("svc bank login");
            e1.setUsername("dummyusername1");
            e1.setPassword("dummypassword1");
            e1.setUrl("http://www.dummy1.com");
            e1.setNotes("Some dummy note");
            e1.setExpiryTime(currentCalender.getTime());
            g.addEntry(e1);
            Entry e2 = database.newEntry("Icici Bank login");
            e2.setUsername("dummyusername2");
            e2.setPassword("dummypassword2");
            e2.setUrl("http://www.dummy2.com");
            e2.setNotes("Some dummy note");
            e2.setExpiryTime(currentCalender.getTime());
            g.addEntry(e2);
            Group g1 = database.newGroup("Bank - Dad");
            Entry e3 = database.newEntry("hdfc - login");
            e3.setUsername("dummyusername1");
            e3.setPassword("dummypassword1");
            e3.setUrl("http://www.dummy1.com");
            e3.setNotes("Some dummy note");
            e3.setExpiryTime(currentCalender.getTime());
            g1.addEntry(e3);
            Entry e4 = database.newEntry("Bank 2");
            e4.setUsername("dummyusername2");
            e4.setPassword("dummypassword2");
            e4.setUrl("http://www.dummy2.com");
            e4.setNotes("Some dummy note");
            e4.setExpiryTime(currentCalender.getTime());
            g1.addEntry(e4);
            g.addGroup(g1);
            rootGroup.addGroup(g);
        }
        {
            Group g = database.newGroup("IT-Company");
            Entry e1 = database.newEntry("RHEL Database");
            e1.setUsername("dummyusername1");
            e1.setPassword("dummypassword1");
            e1.setUrl("http://www.dummy1.com");
            e1.setNotes("Some dummy note");
            e1.setExpiryTime(currentCalender.getTime());
            g.addEntry(e1);
            Entry e2 = database.newEntry("MySQL Database");
            e2.setUsername("dummyusername2");
            e2.setPassword("dummypassword2");
            e2.setUrl("http://www.dummy2.com");
            e2.setNotes("Some dummy note");
            e2.setExpiryTime(currentCalender.getTime());
            g.addEntry(e2);

            Group g1 = database.newGroup("remote_system_1_user");
            Entry e3 = database.newEntry("User 1");
            e3.setUsername("dummyusername1");
            e3.setPassword("dummypassword1");
            e3.setUrl("http://www.dummy1.com");
            e3.setNotes("Some dummy note");
            e3.setExpiryTime(currentCalender.getTime());
            g1.addEntry(e3);

            Entry e4 = database.newEntry("User 2");
            e4.setUsername("dummyusername2");
            e4.setPassword("dummypassword2");
            e4.setUrl("http://www.dummy2.com");
            e4.setNotes("Some dummy note");
            e4.setExpiryTime(currentCalender.getTime());
            g1.addEntry(e4);
            g.addGroup(g1);
            rootGroup.addGroup(g);
        }

        boolean forDebug = false;
        if (forDebug) {
            for (int i = 5; i < 50; i++) {
                Group g = database.newGroup("Dummy Company database Group");
                Entry e1 = database.newEntry("Dummy RHEL Database 1 - " + i);
                e1.setUsername("dummyusername1-" + i);
                e1.setPassword("dummypassword1-" + i);
                e1.setUrl("http://www.dummy1-" + i + ".com");
                e1.setNotes("Some dummy note");
                e1.setExpiryTime(currentCalender.getTime());
                g.addEntry(e1);
                Entry e2 = database.newEntry("Dummy MySQL Database 2 - " + i);
                e2.setUsername("dummyusername2- " + i);
                e2.setPassword("dummypassword2- " + i);
                e2.setUrl("http://www.dummy2-" + i + " .com");
                e2.setNotes("Some dummy note");
                e2.setExpiryTime(currentCalender.getTime());
                g.addEntry(e2);

                Group g1 = database.newGroup("Dummy Company sub Group");
                Entry e3 = database.newEntry("Dummy User 1");
                e3.setUsername("dummyusername1");
                e3.setPassword("dummypassword1");
                e3.setUrl("http://www.dummy1.com");
                e3.setNotes("Some dummy note");
                e3.setExpiryTime(currentCalender.getTime());
                g1.addEntry(e3);

                Entry e4 = database.newEntry("Dummy User 2");
                e4.setUsername("dummyusername2");
                e4.setPassword("dummypassword2");
                e4.setUrl("http://www.dummy2.com");
                e4.setNotes("Some dummy note");
                e4.setExpiryTime(currentCalender.getTime());
                g1.addEntry(e4);
                g.addGroup(g1);

                rootGroup.addGroup(g);
            }
        }
        return database;
    }

    private void Validate() throws KpCustomException {
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;
        if (kdbxFileUri == null) {
            throw new KpCustomException(R.string.emptyFileError);
        }
        if (kdbxPasswordET.getText() != null) {
            String kdbxPassword = kdbxPasswordET.getText().toString();
            if (kdbxPassword == null || kdbxPassword.length() <= 0) {
                throw new KpCustomException(R.string.emptyPasswordError);
            }
        } else {
            throw new KpCustomException(R.string.emptyPasswordError);
        }

    }

    private void ValidateCodec() throws KpCustomException {
        if (!Common.isCodecAvailable) {
            throw new KpCustomException(R.string.devInProgress);
        }
    }


    private void showWelcomeMessage(View v) {
        try {
            binding.appNameTextView.setText(R.string.welcomeBackMsg);
        } catch (Exception e) {

        }
        binding.appNameTextView.startAnimation(AnimationUtils.loadAnimation(v.getContext(), androidx.transition.R.anim.abc_grow_fade_in_from_bottom));
    }

    private void setThemeButton() {
        binding.themeFloatBtn.setOnClickListener(v -> {
            ThemeSettingDialogUtil.show(getLayoutInflater(), v, (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE), binding.getRoot().findViewById(R.id.floatGenerateNewPassword), this);
        });
    }

    private void checkCodecAvailable() {
        try {
            org.apache.commons.codec.binary.Base64.encodeBase64String("".getBytes());
            Common.isCodecAvailable = true;
        } catch (NoSuchMethodError e) {
            Common.isCodecAvailable = false;
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
        } catch (Exception e) {
            Common.isCodecAvailable = false;
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
        }
    }

    private void showInfoDialog() {
        new InfoDialogUtil().showInfoDialog(binding.getRoot().getContext());
    }

    private void copyFile() {
        createMainDirectory();
        createSubFilesDirectory();
        File fromTo = new File(subFilesDirPath + File.separator + binding.kdbxFileName.getText().toString());
        if (fromTo.exists()) {
            fromTo.delete();
        } else {
            try {
                fromTo.createNewFile();
                fromTo.setWritable(true, true);
                fromTo.setExecutable(true, true);
                fromTo.setReadable(true, true);
            } catch (IOException e) {
            }
        }

        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(kdbxFileUri);
        } catch (FileNotFoundException e) {
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fromTo);
            ByteStreams.copy(inputStream, outputStream);
        } catch (Exception e) {
            Log.i("JKEEPASS", "Error copying file : " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {

            }
        }
    }

    private void createMainDirectory() {
        Log.i("JKEEPASS", "dirPath: " + dirPath);
        if (dirPath == null) {
            Log.i("JKEEPASS", "Null dirPath: " + dirPath);
        } else {
            File projDir = new File(dirPath);
            if (!projDir.exists()) {
                projDir.mkdirs();
                Log.i("JKEEPASS", "Created dirPath: " + dirPath);
            } else {
                Log.i("JKEEPASS", "Exists dirPath: " + dirPath);
            }
        }
    }

    private void createSubFilesDirectory() {
        Log.i("JKEEPASS", "subFilesDirPath: " + subFilesDirPath);
        if (subFilesDirPath == null) {
            Log.i("JKEEPASS", "Null subFilesDirPath: " + subFilesDirPath);
        } else {
            File subFilesDir = new File(subFilesDirPath);
            if (!subFilesDir.exists()) {
                subFilesDir.mkdirs();
                Log.i("JKEEPASS", "Created subFilesDirPath: " + subFilesDirPath);
            } else {
                Log.i("JKEEPASS", "Exists subFilesDirPath: " + subFilesDirPath);
            }
        }
    }

    private void fetchAndShowFiles() {
        runOnUiThread(() -> {
            binding.listDatabasesLinerLayout.removeAllViews();
        });
        createMainDirectory();
        createSubFilesDirectory();
        File subFilesDir = new File(subFilesDirPath);
        File[] files = subFilesDir.listFiles();
        Arrays.sort(files);
        if (files != null && files.length > 0) {
            runOnUiThread(() -> {
                binding.justImportCreateTextView.setText("Loading ... " + files.length);
            });

            int fCount = 0;
            for (File f : files) {
                Util.sleepFor100Sec();
                addFileLayout(f);
                fCount++;
                int finalFCount = fCount;
                runOnUiThread(() -> {
                    binding.justImportCreateTextView.setText("Loading ... done " + finalFCount + " of " + files.length);
                });
            }
        }
        showDeclaration(files);
    }

    @SuppressLint("ResourceType")
    private void showDeclaration(File[] files) {
        if (files == null || files.length <= 0) {
            runOnUiThread(() -> {
                binding.justImportCreateTextView.setText(R.string.importDatabase);
                binding.justImportCreateTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            });
        } else {
            runOnUiThread(() -> {
                binding.justImportCreateTextView.setText(R.string.importDeclaration);
                binding.justImportCreateTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
            });
        }
    }

    @SuppressLint("ResourceType")
    private void addFileLayout(File f) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_kdbx_files_view, null);
        ((TextView) viewToLoad.findViewById(R.id.databaseName)).setText(f.getName());
        viewToLoad.setTag(f.getPath());
        LinearLayout databaseNameLinearLayout = viewToLoad.findViewById(R.id.databaseNameLinearLayout);
        /*try {
            LayerDrawable layerDrawable = (LayerDrawable) getResources()
                    .getDrawable(R.drawable.bottom_color_style);
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.itemId);
            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());

            gradientDrawable.setStroke(layerDrawable.getIntrinsicWidth(), getResources().getColor(R.color.kp_red));
            databaseNameLinearLayout.setBackground(layerDrawable);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        databaseNameLinearLayout.setOnClickListener(v -> {
            kdbxFileUri = Uri.fromFile(f);
            binding.kdbxFileName.setText(f.getName());
            showOpenDialog(f.getName(), v);
        });
        viewToLoad.findViewById(R.id.databaseMoreOption).setOnClickListener(v -> {
            Pair<BottomSheetDialog, ArrayList<LinearLayout>> bsd = BottomMenuUtil.getDbMenuOptions(f.getName(), v.getContext());
            bsd.first.show();
            bsd.second.get(0).setOnClickListener(view -> {
                bsd.first.dismiss();
                Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> editDatabaseNameDialog = DatabaseCreateDialogUtil.getConfirmDialogEditName(viewToLoad.getContext(), f.getName());
                editDatabaseNameDialog.second.setOnClickListener(v1 -> {
                    editDatabaseNameDialog.first.dismiss();
                    if (!Common.isCodecAvailable) {
                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.devInProgress, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    } else if (editDatabaseNameDialog.third.getText() == null || editDatabaseNameDialog.third.getText().toString().length() <= 0) {
                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.enterDatabaseName, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    } else {
                        String dbName = editDatabaseNameDialog.third.getText().toString();
                        if (!dbName.endsWith("kdbx")) {
                            dbName = dbName + ".kdbx";
                        }
                        File fromTo = new File(subFilesDirPath + File.separator + dbName);
                        f.renameTo(fromTo);
                        if (v1 != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                        }
                        fetchAndShowFiles();
                        editDatabaseNameDialog.first.dismiss();
                    }
                });
                editDatabaseNameDialog.first.show();
            });
            bsd.second.get(1).setOnClickListener(view -> {
                bsd.first.dismiss();
                Quadruple<BottomSheetDialog, MaterialButton, TextInputEditText, TextInputEditText> confirmDialog = DatabaseCreateDialogUtil.getConfirmDialogChangePassword(f.getName(), binding.getRoot().getContext());
                confirmDialog.second.setOnClickListener(v1 -> {
                    confirmDialog.first.dismiss();
                    if (!Common.isCodecAvailable) {
                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.devInProgress, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    } else if (confirmDialog.third.getText() == null || confirmDialog.third.getText().toString().length() <= 0) {
                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.enterPassword, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    } else if (confirmDialog.fourth.getText() == null || confirmDialog.fourth.getText().toString().length() <= 0) {
                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.enterPassword, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    } else {
                        if (v1 != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                        }
                        confirmDialog.first.dismiss();
                        AlertDialog changePasswordDialog = ProgressDialogUtil.getSaving(LayoutInflater.from(binding.getRoot().getRootView().getContext()), binding.getRoot().getRootView().getContext());
                        ProgressDialogUtil.showSavingDialog(changePasswordDialog);
                        ProgressDialogUtil.setSavingProgress(changePasswordDialog, 10);
                        new Thread(() -> {
                            {
                                try {
                                    kdbxFileUri = Uri.fromFile(f);
                                    KdbxCreds creds = new KdbxCreds(confirmDialog.third.getText().toString().getBytes());
                                    Database<?, ?, ?, ?> database = null;
                                    updateSavingProgress(changePasswordDialog, 20);
                                    InputStream inputStream = getContentResolver().openInputStream(kdbxFileUri);
                                    database = SimpleDatabase.load(creds, inputStream);
                                    updateSavingProgress(changePasswordDialog, 50);
                                    if (database != null) {
                                        OutputStream fileOutputStream = getContentResolver().openOutputStream(kdbxFileUri, "wt");
                                        KdbxCreds newCreds = new KdbxCreds(confirmDialog.fourth.getText().toString().getBytes());
                                        database.save(newCreds, fileOutputStream);
                                        updateSavingProgress(changePasswordDialog, 60);
                                        fetchAndShowFiles();
                                        kdbxFileUri = null;
                                        updateSavingProgress(changePasswordDialog, 80);
                                        updateSavingProgress(changePasswordDialog, 90);
                                        dismissSavingDialog(changePasswordDialog);
                                    } else {
                                        dismissSavingDialog(changePasswordDialog);
                                        runOnUiThread(() -> {
                                            ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.noDBError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                                        });
                                    }
                                } catch (Exception e) {
                                    dismissSavingDialog(changePasswordDialog);
                                    runOnUiThread(() -> {
                                        ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), R.string.noDBError, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                                    });
                                }
                            }
                            ;
                        }).start();
                    }
                });
                confirmDialog.first.show();
            });
            bsd.second.get(2).setOnClickListener(view -> {
                bsd.first.dismiss();
                Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(getLayoutInflater(), viewToLoad.getContext());
                confirmDialog.second.setOnClickListener(viewObj -> {
                    f.delete();
                    fetchAndShowFiles();
                    confirmDialog.first.dismiss();
                });
                ConfirmDialogUtil.showDialog(confirmDialog.first);
            });
        });

        TextView databaseMoreInfo = viewToLoad.findViewById(R.id.databaseMoreInfo);
        databaseMoreInfo.setText("Modified: " + Util.convertDateToStringOnlyDate(f.lastModified()) + " ");
        databaseMoreInfo.setTextSize(TypedValue.COMPLEX_UNIT_PT, 4);
        CardView databaseNameCardView = viewToLoad.findViewById(R.id.databaseNameCardView);
        runOnUiThread(() -> {
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(viewToLoad.getContext(), R.animator.anim_bottom), Common.ANIMATION_TIME);
            databaseNameCardView.setLayoutAnimation(lac);
            binding.listDatabasesLinerLayout.addView(viewToLoad);
        });

    }

    private void dismissSavingDialog(AlertDialog changePasswordDialog) {
        runOnUiThread(() -> {
            ProgressDialogUtil.dismissSavingDialog(changePasswordDialog);
        });
    }

    private void updateSavingProgress(AlertDialog changePasswordDialog, int i) {
        runOnUiThread(() -> {
            ProgressDialogUtil.setSavingProgress(changePasswordDialog, i);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private boolean checkAndGetPermission(View v, Activity activity) {
        boolean isOK = IS_READ_EXTERNAL_STORAGE_RECEIVED;
        if (!isOK) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            } else {
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_IMAGES}, READ_EXTERNAL_STORAGE);
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            }
        }
        IS_READ_EXTERNAL_STORAGE_RECEIVED = isOK;
        return isOK;
    }

    private boolean checkAndGetAlarmPermission(View v, Activity activity) {
        Log.i("JKEEPASS", "checkAndGetAlarmPermission");
        boolean isOK = IS_ALARM_PERMISSION_RECEIVED;
        if (!isOK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.USE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.USE_EXACT_ALARM}, ALARM);
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    //ToastUtil.showToast(getLayoutInflater(), v, R.string.NotificationPermissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.POST_NOTIFICATIONS}, ALARM);
                }
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    //ToastUtil.showToast(getLayoutInflater(), v, R.string.NotificationPermissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    isOK = false;
                }
            } else {
                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("JKEEPASS", "checkAndGetAlarmPermission not granted");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, ALARM);
                    isOK = true;
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    //ToastUtil.showToast(getLayoutInflater(), v, R.string.NotificationPermissionNotGranted, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                }
            }
        }
        IS_ALARM_PERMISSION_RECEIVED = isOK;
        Log.i("JKEEPASS", "checkAndGetAlarmPermission isOK is " + isOK);
        return isOK;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("JKEEPASS", "onRequestPermissionsResult");
        if (requestCode == ALARM) {
            if (!Arrays.asList(grantResults).contains(PackageManager.PERMISSION_DENIED)) {
                startAlarmBroadcastReceiver(this);
            }
        } else if (requestCode == READ_EXTERNAL_STORAGE) {
            binding.createBtn.performClick();
        }
    }

    public void startAlarmBroadcastReceiver(Context context) {
        try {
            if (checkAndGetAlarmPermission(binding.getRoot().getRootView(), this)) {
                boolean isAvailable = false;
                Intent _intent = new Intent(context, AlarmBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, _intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                //PendingIntent pendingIntent = PendingIntent.getService(context, 1, _intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (pendingIntent != null && alarmManager != null) {
                    //alarmManager.cancel(pendingIntent);
                    isAvailable = true;
                }
                if (!isAvailable) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, 10);
                    calendar.set(Calendar.MINUTE, 00);
                    calendar.set(Calendar.SECOND, 00);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String formattedDate = simpleDateFormat.format(calendar.getTime());
                        Log.i("JKEEPASS", " Not Available and Notification set. " + formattedDate);
                        //ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), "" + (isCancelled ? " Cancelled and" : "") + " Notification set. " + formattedDate, binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
                    }
                } else {
                    Log.i("JKEEPASS", "Notification is available and set to " + alarmManager);
                }
            }
        } catch (Exception e) {
            Log.i("JKEEPASS", "Notification set error ." + e.getMessage());
            //ToastUtil.showToast(getLayoutInflater(), binding.getRoot().getRootView(), "Notification set error ." + e.getMessage(), binding.getRoot().findViewById(R.id.floatGenerateNewPassword));
        }
    }

    private void showOpenDialog(String name, View v) {
        Triplet<BottomSheetDialog, MaterialButton, TextInputEditText> openTriplet = DatabaseCreateDialogUtil.getOpenDialog(name, v.getContext());
        openTriplet.first.show();
        TextInputEditText kdbxListFilePassword = openTriplet.third;
        kdbxListFilePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (kdbxListFilePassword.getText() != null) {
                    binding.kdbxFileGotPassword.setText(kdbxListFilePassword.getText().toString());
                }
            }
        });
        openTriplet.second.setOnClickListener(view -> {
            openTriplet.first.dismiss();
            final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), LoadActivity.this);
            ProgressDialogUtil.showLoadingDialog(alertDialog);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    process(alertDialog, binding.getRoot().getRootView());
                }
            }).start();
        });
    }
}