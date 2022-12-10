package org.j_keepass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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

public class LoadActivity extends AppCompatActivity {
    public static final int PICK_FILE_OPEN_RESULT_CODE = 1;
    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;
    private ActivityLoadBinding binding;
    private Uri kdbxFileUri = null;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private int currentNightMode = Configuration.UI_MODE_NIGHT_NO;
    private String dirPath = null;
    private String subFilesDirPath = null;
    boolean isFileAvailable = false;

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

        Dialog banner = BannerDialogUtil.getBanner(getLayoutInflater(), this);
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
                Thread.sleep(3000);
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
                closeKeyboard();
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
        boolean useOldCreate = false;
        if (useOldCreate) {
            createBtn.setOnClickListener(new View.OnClickListener() {
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
                        Intent chooseFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                        chooseFile.setType("*/*");
                        chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        chooseFile.putExtra(Intent.EXTRA_TITLE, "database.kdbx");

                        chooseFile = Intent.createChooser(chooseFile, "Choose a folder");
                        startActivityForResult(chooseFile, PICK_FOLDER_OPEN_RESULT_CODE);
                    } else {
                        ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
                    }
                }
            });
        } else {
            createBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                                    confirmDialog.first.dismiss();
                                    closeKeyboard();
                                    fetchAndShowFiles();
                                } catch (Exception e) {
                                    ToastUtil.showToast(getLayoutInflater(), v1, e.getMessage());
                                }
                            }
                        }

                    });
                    DatabaseCreateDialogUtil.showDialog(confirmDialog.first);
                }
            });
        }
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
                    Log.i("JKeepass","Flags: "+data.getFlags());
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
            }else
            {
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

        if( !proceed )
        {
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
        Group g = database.newGroup("Dummy Group");
        Entry e1 = database.newEntry("Dummy Entry 1");
        g.addEntry(e1);
        Entry e2 = database.newEntry("Dummy Entry 2");
        g.addEntry(e2);
        Group rootGroup = database.getRootGroup();
        rootGroup.setName("Root");
        rootGroup.addGroup(g);
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

    private void closeKeyboard() {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
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
            currentNightMode = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    // Night mode is not active on device
                    binding.themeFloatBtn.setImageResource(R.drawable.ic_dark_mode_fill0_wght300_grad_25_opsz24);
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    // Night mode is active on device
                    binding.themeFloatBtn.setImageResource(R.drawable.ic_light_mode_fill0_wght300_grad_25_opsz24);
                    break;
            }
        } catch (Exception e) {
            ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.themeModeInfoNotGotError);
        }

        binding.themeFloatBtn.setOnClickListener(v -> {
            try {
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        binding.themeFloatBtn.setImageResource(R.drawable.ic_light_mode_fill0_wght300_grad_25_opsz24);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        binding.themeFloatBtn.setImageResource(R.drawable.ic_dark_mode_fill0_wght300_grad_25_opsz24);
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
        final Dialog dialog = new Dialog(LoadActivity.this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.info_layout);
        ImageButton llink = dialog.findViewById(R.id.llink);
        llink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.linkedin.com/in/jayesh-ganatra-76051056"));
                startActivity(intent);
            }
        });

        ImageButton elink = dialog.findViewById(R.id.elink);
        elink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://flowcv.me/jayesh-ganatra"));
                startActivity(intent);
            }
        });

        ImageButton glink = dialog.findViewById(R.id.glink);
        glink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/dev?id=7560962222107226464"));
                startActivity(intent);
            }
        });
        FloatingActionButton closeInfoBtn = dialog.findViewById(R.id.floatCloseInfoBtn);
        closeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void copyFile() {
        createMainDirectory();
        createSubFilesDirectory();
        File fromTo = new File(subFilesDirPath + File.separator + binding.kdbxFileName.getText().toString());
        if (fromTo.exists()) {
            fromTo.delete();
        }else
        {
            try {
                fromTo.createNewFile();
                fromTo.setWritable(true, true);
                fromTo.setExecutable(true,true);
                fromTo.setReadable(true,true);
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
    }

    @SuppressLint("ResourceType")
    private void addFileLayout(File f) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewToLoad = inflater.inflate(R.layout.activity_list_kdbx_files_view, null);
        ((TextView) viewToLoad.findViewById(R.id.databaseName)).setText(f.getName());
        LinearLayout kdbxListFilePasswordLayout = viewToLoad.findViewById(R.id.kdbxListFilePasswordLayout);
        kdbxListFilePasswordLayout.setVisibility(View.GONE);
        LinearLayout databaseNameLinearLayout = viewToLoad.findViewById(R.id.databaseNameLinearLayout);
        databaseNameLinearLayout.setOnClickListener(v -> {
            if (kdbxListFilePasswordLayout.getVisibility() == View.VISIBLE) {
                kdbxListFilePasswordLayout.setVisibility(View.GONE);
                kdbxFileUri = null;
                binding.kdbxFileName.setText("");
            } else {
                kdbxListFilePasswordLayout.setVisibility(View.VISIBLE);
                kdbxFileUri = Uri.fromFile(f);
                binding.kdbxFileName.setText(f.getName());
            }
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
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(this, R.animator.anim_slide_in_left), 0.5f);
        binding.listDatabasesLinerLayout.setLayoutAnimation(lac);
        binding.listDatabasesLinerLayout.addView(viewToLoad);
    }
}