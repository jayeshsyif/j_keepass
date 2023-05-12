package org.j_keepass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.io.ByteStreams;

import org.j_keepass.databinding.ActivityLoadBinding;
import org.j_keepass.util.BannerDialogUtil;
import org.j_keepass.util.Common;
import org.j_keepass.util.ConfirmDialogUtil;
import org.j_keepass.util.DatabaseCreateDialogUtil;
import org.j_keepass.util.InfoDialogUtil;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.Penta;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.j_keepass.util.Triplet;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

public class LoadActivity extends AppCompatActivity {
    public static final int PICK_FILE_OPEN_RESULT_CODE = 1;
    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;
    private ActivityLoadBinding binding;
    private Uri kdbxFileUri = null;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private int defaultThemeCode = Configuration.UI_MODE_NIGHT_NO;
    private String dirPath = null;
    private String subFilesDirPath = null;
    boolean isFileAvailable = false;
    Dialog banner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        if (banner != null) {
            Log.i("JKEEPASS", "banner not null");
        } else {
            Log.i("JKEEPASS", "banner null");
        }
        banner = BannerDialogUtil.getBanner(getLayoutInflater(), this);
        banner.show();
        if (isFileAvailable) {
            loadFile();
        }
        dirPath = getFilesDir().getPath() + File.separator + "org.j_keepass";
        subFilesDirPath = getFilesDir().getPath() + File.separator + "org.j_keepass" + File.separator + "kdbxfiles";
        setEvents();

        if (isFileAvailable) {
            binding.openImportLayout.setVisibility(View.GONE);
            binding.kdbxFileName.setVisibility(View.VISIBLE);
            binding.kdbxFileGotPasswordLayout.setVisibility(View.VISIBLE);
            binding.justDatabaseText.setVisibility(View.GONE);
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterPassword);
        } else {
            binding.kdbxFileName.setVisibility(View.GONE);
            binding.openImportLayout.setVisibility(View.VISIBLE);
            binding.kdbxFileName.setVisibility(View.GONE);
            binding.kdbxFileGotPasswordLayout.setVisibility(View.GONE);
            binding.justDatabaseText.setVisibility(View.VISIBLE);
        }

        Thread bannerThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //do nothing
            }
            banner.dismiss();
            loadAfterDialog();
        });
        if (!isFileAvailable) {
            bannerThread.start();
        } else {
            banner.dismiss();
            loadAfterDialog();
        }
    }

    private void loadAfterDialog() {
        runOnUiThread(() -> {
            showWelcomeMessage(binding.getRoot());
            if (!isFileAvailable) {
                fetchAndShowFiles();
            }
        });
    }

    private void setEvents() {
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;

        kdbxPasswordET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                if (binding.getRoot().getContext() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), LoadActivity.this);
                ProgressDialogUtil.showLoadingDialog(alertDialog);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        process(alertDialog, v);
                    }
                }).start();
            }
        });
        binding.importBtn.setOnClickListener(new View.OnClickListener() {
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
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
                }
            }
        });
        MaterialButton createBtn = binding.createBtn;
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isOK = false;

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(LoadActivity.this, new String[]{
                                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE);
                }

                if (ContextCompat.checkSelfPermission(binding.getRoot().getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    isOK = true;
                } else {
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
                }
                if (isOK) {
                    Penta<AlertDialog, MaterialButton, MaterialButton, TextInputEditText, TextInputEditText> confirmDialog = DatabaseCreateDialogUtil.getConfirmDialog(getLayoutInflater(), binding.getRoot().getContext());

                    confirmDialog.second.setOnClickListener(v1 -> {
                        if (!Common.isCodecAvailable) {
                            ToastUtil.showToast(getLayoutInflater(), v1, R.string.devInProgress);
                        } else if (confirmDialog.fourth.getText() == null || confirmDialog.fourth.getText().toString().length() <= 0) {
                            ToastUtil.showToast(getLayoutInflater(), v1, R.string.enterDatabaseName);
                        } else if (confirmDialog.fifth.getText() == null || confirmDialog.fifth.getText().toString().length() <= 0) {
                            ToastUtil.showToast(getLayoutInflater(), v1, R.string.enterPassword);
                        } else {
                            String dbName = confirmDialog.fourth.getText().toString();
                            if (!dbName.endsWith("kdbx")) {
                                dbName = dbName + ".kdbx";
                            }
                            File fromTo = new File(subFilesDirPath + File.separator + dbName);
                            if (fromTo.exists()) {
                                fromTo.delete();
                            } else {
                                try {
                                    fromTo.createNewFile();
                                    kdbxFileUri = Uri.fromFile(fromTo);
                                    KdbxCreds creds = new KdbxCreds(confirmDialog.fifth.getText().toString().getBytes());
                                    Database<?, ?, ?, ?> database = getDummyDatabase();
                                    OutputStream fileOutputStream = getContentResolver().openOutputStream(kdbxFileUri, "wt");
                                    database.save(creds, fileOutputStream);
                                    fetchAndShowFiles();
                                    kdbxFileUri = null;
                                    if (v1 != null) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(v1.getWindowToken(), 0);
                                    }
                                    confirmDialog.first.dismiss();
                                } catch (Exception e) {
                                    ToastUtil.showToast(getLayoutInflater(), v1, e.getMessage());
                                }
                            }
                        }

                    });
                    DatabaseCreateDialogUtil.showDialog(confirmDialog.first);
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
                    fetchAndShowFiles();
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
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError);
            }
            try {
                Cursor returnCursor =
                        getContentResolver().query(kdbxFileUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                fileName = returnCursor.getString(nameIndex);
                binding.kdbxFileName.setText(fileName);
            } catch (Exception e) {
                fileName = e.getMessage();
            }
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
            ToastUtil.showToast(getLayoutInflater(), v, e);
            proceed = false;
        }
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;
        ProgressDialogUtil.setLoadingProgress(alertDialog, 30);
        if (proceed) {
            if (kdbxFileUri == null) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, R.string.emptyFileError);
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
                ToastUtil.showToast(getLayoutInflater(), v, R.string.invalidFileError + " " + e.getMessage());
            }
            ProgressDialogUtil.setLoadingProgress(alertDialog, 60);
            if (inputStream != null) {
                try {
                    database = SimpleDatabase.load(creds, inputStream);
                } catch (Exception e) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    proceed = false;
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.invalidFileError + " " + e.getMessage());
                }
                if (database == null) {
                    ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.noDBError);
                    proceed = false;
                }
            }
            ProgressDialogUtil.setLoadingProgress(alertDialog, 90);
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
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.unableToNavigateError);
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
            ToastUtil.showToast(getLayoutInflater(), v, e);
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
                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage());
            } catch (Exception e) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, e.getMessage());
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
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.unableToNavigateError);
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
            Group g = database.newGroup("Dummy Bank Group");
            Entry e1 = database.newEntry("Dummy Bank 1");
            e1.setUsername("dummyusername1");
            e1.setPassword("dummypassword1");
            e1.setUrl("http://www.dummy1.com");
            e1.setNotes("Some dummy note");
            e1.setExpiryTime(currentCalender.getTime());
            g.addEntry(e1);
            Entry e2 = database.newEntry("Dummy Bank 2");
            e2.setUsername("dummyusername2");
            e2.setPassword("dummypassword2");
            e2.setUrl("http://www.dummy2.com");
            e2.setNotes("Some dummy note");
            e2.setExpiryTime(currentCalender.getTime());
            g.addEntry(e2);
            Group g1 = database.newGroup("Dummy Local Bank Sub Group");
            Entry e3 = database.newEntry("Dummy Sub User 1");
            e3.setUsername("dummyusername1");
            e3.setPassword("dummypassword1");
            e3.setUrl("http://www.dummy1.com");
            e3.setNotes("Some dummy note");
            e3.setExpiryTime(currentCalender.getTime());
            g1.addEntry(e3);
            Entry e4 = database.newEntry("Dummy Sub User 2");
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
            Group g = database.newGroup("Dummy Company database Group");
            Entry e1 = database.newEntry("Dummy RHEL Database 1");
            e1.setUsername("dummyusername1");
            e1.setPassword("dummypassword1");
            e1.setUrl("http://www.dummy1.com");
            e1.setNotes("Some dummy note");
            e1.setExpiryTime(currentCalender.getTime());
            g.addEntry(e1);
            Entry e2 = database.newEntry("Dummy MySQL Database 2");
            e2.setUsername("dummyusername2");
            e2.setPassword("dummypassword2");
            e2.setUrl("http://www.dummy2.com");
            e2.setNotes("Some dummy note");
            e2.setExpiryTime(currentCalender.getTime());
            g.addEntry(e2);

            Group g1 = database.newGroup("Dummy Company User Group");
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
        return database;
    }

    private void Validate() throws KpCustomException {
        TextInputEditText kdbxPasswordET = binding.kdbxFileGotPassword;
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
        try {
            defaultThemeCode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            SharedPreferences sharedPref = LoadActivity.this.getPreferences(Context.MODE_PRIVATE);
            int themeCode = sharedPref.getInt(getString(R.string.themeCode), 0);
            if (themeCode > 0) {
                defaultThemeCode = themeCode;
            }
            switch (defaultThemeCode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    // Night mode is not active on device
                    binding.themeFloatBtn.setImageResource(R.drawable.ic_dark_mode_fill0_wght300_grad_25_opsz24);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    // Night mode is active on device
                    binding.themeFloatBtn.setImageResource(R.drawable.ic_light_mode_fill0_wght300_grad_25_opsz24);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case 3:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.themeModeInfoNotGotError);
        }

        binding.themeFloatBtn.setOnClickListener(v -> {
            try {
                SharedPreferences sharedPref = LoadActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                switch (defaultThemeCode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        binding.themeFloatBtn.setImageResource(R.drawable.ic_light_mode_fill0_wght300_grad_25_opsz24);
                        editor.putInt(getString(R.string.themeCode), Configuration.UI_MODE_NIGHT_YES);
                        editor.apply();
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        binding.themeFloatBtn.setImageResource(R.drawable.ic_dark_mode_fill0_wght300_grad_25_opsz24);
                        editor.putInt(getString(R.string.themeCode), Configuration.UI_MODE_NIGHT_NO);
                        editor.apply();
                        break;
                }
            } catch (Exception e) {
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.themeModeInfoNotGotError);
            }
        });
    }

    private void checkCodecAvailable() {
        try {
            org.apache.commons.codec.binary.Base64.encodeBase64String("".getBytes());
            Common.isCodecAvailable = true;
        } catch (NoSuchMethodError e) {
            Common.isCodecAvailable = false;
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError);
        } catch (Exception e) {
            Common.isCodecAvailable = false;
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError);
        }
    }

    private void showInfoDialog() {
        final Dialog dialog = new InfoDialogUtil().getInfoDialog(binding.getRoot().getContext(), getLayoutInflater());
        dialog.show();
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
        binding.listDatabasesLinerLayout.removeAllViews();
        createMainDirectory();
        createSubFilesDirectory();
        File subFilesDir = new File(subFilesDirPath);
        File[] files = subFilesDir.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                addFileLayout(f);
            }
        }
        showDeclaration(files);
    }

    @SuppressLint("ResourceType")
    private void showDeclaration(File[] files) {
        if (files == null || files.length <= 0) {
            binding.justImportCreateTextView.setText(R.string.importDatabase);
            binding.justImportCreateTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
        } else {
            binding.justImportCreateTextView.setText(R.string.importDeclaration);
            binding.justImportCreateTextView.startAnimation(AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.animator.anim_bottom));
        }
    }

    @SuppressLint("ResourceType")
    private void addFileLayout(File f) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_kdbx_files_view, null);
        ((TextView) viewToLoad.findViewById(R.id.databaseName)).setText(f.getName());
        viewToLoad.setTag(f.getPath());
        LinearLayout kdbxListFilePasswordLayout = viewToLoad.findViewById(R.id.kdbxListFilePasswordLayout);
        kdbxListFilePasswordLayout.setVisibility(View.GONE);
        LinearLayout databaseNameLinearLayout = viewToLoad.findViewById(R.id.databaseNameLinearLayout);
        databaseNameLinearLayout.setOnClickListener(v -> {
            runOnUiThread(() -> {
                final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), inflater.getContext());
                ProgressDialogUtil.showLoadingDialog(alertDialog);
                ProgressDialogUtil.setLoadingProgress(alertDialog, 10);

                if (kdbxFileUri != null) {
                    int ch = binding.listDatabasesLinerLayout.getChildCount();
                    if (ch > 0) {
                        for (int i = 0; i < ch; i++) {
                            if (binding.listDatabasesLinerLayout.getChildAt(i).getTag() != null) {
                                if (binding.listDatabasesLinerLayout.getChildAt(i).getTag().toString().equals(kdbxFileUri.getPath())) {
                                    kdbxFileUri = null;
                                    binding.listDatabasesLinerLayout.getChildAt(i).findViewById(R.id.databaseNameLinearLayout).performClick();
                                    break;
                                }
                            }
                        }
                    }
                }
                ProgressDialogUtil.setLoadingProgress(alertDialog, 50);
                if (kdbxListFilePasswordLayout.getVisibility() == View.VISIBLE) {
                    kdbxListFilePasswordLayout.setVisibility(View.GONE);
                    kdbxFileUri = null;
                    binding.kdbxFileName.setText("");
                } else {
                    kdbxListFilePasswordLayout.setVisibility(View.VISIBLE);
                    kdbxFileUri = Uri.fromFile(f);
                    binding.kdbxFileName.setText(f.getName());
                }
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
            });
        });
        TextInputEditText kdbxListFilePassword = viewToLoad.findViewById(R.id.kdbxListFilePassword);
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
        ImageButton databaseDeleteBtn = viewToLoad.findViewById(R.id.databaseDeleteBtn);
        databaseDeleteBtn.setOnClickListener(v -> {

            Triplet<AlertDialog, MaterialButton, MaterialButton> confirmDialog = ConfirmDialogUtil.getConfirmDialog(getLayoutInflater(), this);
            confirmDialog.second.setOnClickListener(viewObj -> {
                f.delete();
                fetchAndShowFiles();
                confirmDialog.first.dismiss();
            });
            ConfirmDialogUtil.showDialog(confirmDialog.first);
        });
        ImageButton databaseEditBtn = viewToLoad.findViewById(R.id.databaseEditBtn);
        databaseEditBtn.setOnClickListener(v -> {
            Penta<AlertDialog, MaterialButton, MaterialButton, TextInputEditText, TextInputEditText> editDatabaseNameDialog = DatabaseCreateDialogUtil.getConfirmDialogEditName(getLayoutInflater(), this, f.getName());
            editDatabaseNameDialog.second.setOnClickListener(v1 -> {
                if (!Common.isCodecAvailable) {
                    ToastUtil.showToast(getLayoutInflater(), v1, R.string.devInProgress);
                } else if (editDatabaseNameDialog.fourth.getText() == null || editDatabaseNameDialog.fourth.getText().toString().length() <= 0) {
                    ToastUtil.showToast(getLayoutInflater(), v1, R.string.enterDatabaseName);
                } else {
                    String dbName = editDatabaseNameDialog.fourth.getText().toString();
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
            DatabaseCreateDialogUtil.showDialog(editDatabaseNameDialog.first);
        });
        CardView databaseNameCardView = viewToLoad.findViewById(R.id.databaseNameCardView);
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_bottom), Common.ANIMATION_TIME);
        databaseNameCardView.setLayoutAnimation(lac);
        binding.listDatabasesLinerLayout.addView(viewToLoad);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle
            outPersistentState) {
        if (banner != null && banner.isShowing()) {
            banner.dismiss();
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (banner != null && banner.isShowing()) {
            banner.dismiss();
        }
        super.onSaveInstanceState(outState);
    }
}