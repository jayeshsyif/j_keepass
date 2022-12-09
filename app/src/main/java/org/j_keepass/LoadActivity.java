package org.j_keepass;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.j_keepass.databinding.ActivityLoadBinding;
import org.j_keepass.util.BannerDialogUtil;
import org.j_keepass.util.Common;
import org.j_keepass.util.KpCustomException;
import org.j_keepass.util.ProgressDialogUtil;
import org.j_keepass.util.ToastUtil;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class LoadActivity extends AppCompatActivity {
    public static final int PICK_FILE_OPEN_RESULT_CODE = 1;
    public static final int PICK_FOLDER_OPEN_RESULT_CODE = 2;
    private ActivityLoadBinding binding;
    private Uri kdbxFileUri = null;
    private static final int READ_EXTERNAL_STORAGE = 100;
    private boolean isCreate = false;
    private int currentNightMode = Configuration.UI_MODE_NIGHT_NO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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


        boolean isFileAvialable = false;
        if (getIntent() != null && getIntent().getData() != null) {
            kdbxFileUri = getIntent().getData();
            isFileAvialable = true;
        } else {
            isFileAvialable = false;
        }

        Common.database = null;

        if (isFileAvialable) {
            loadFile();
            showWelcomeMessage(binding.getRoot());
        } else {
            Dialog banner = BannerDialogUtil.getBanner(getLayoutInflater(), this);
            banner.show();
            Thread bannerThread = new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    //do nothing
                }
                banner.dismiss();
                showWelcomeMessage(binding.getRoot());
            });
            bannerThread.start();
        }


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
                    ToastUtil.showToast(getLayoutInflater(), v, R.string.permissionNotGranted);
                }
            }
        });


        TextInputEditText kdbxPasswordET = binding.kdbxPassword;

        /*kdbxPasswordET.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                closeKeyboard();
                binding.okBtn.performClick();
                binding.okBtn.requestFocus();
                return true;
            }
            return false;
        });*/

        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = ProgressDialogUtil.getLoading(getLayoutInflater(), LoadActivity.this);
                ProgressDialogUtil.showLoadingDialog(alertDialog);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isCreate) {
                            createFile(alertDialog, v);
                        } else {
                            process(alertDialog, v);
                        }
                    }
                }).start();
            }
        });

        MaterialButton createBtn = binding.createBtn;
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
        binding.floatInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
       /* createDummyDataFile(binding.getRoot());
        addDummyLayour(binding.loadScrollviewLinearLayout);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_FILE_OPEN_RESULT_CODE:
                if (resultCode == -1) {
                    isCreate = false;
                    kdbxFileUri = data.getData();
                    loadFile();
                }
                break;
            case PICK_FOLDER_OPEN_RESULT_CODE:
                if (resultCode == -1) {
                    isCreate = true;
                    kdbxFileUri = data.getData();
                    ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.enterPassword);
                    loadFile();
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
            } catch (Exception e) {
                ToastUtil.showToast(getLayoutInflater(), binding.getRoot(), R.string.writePermissionNotGotError);
            }
            try {
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
        TextInputEditText kdbxPasswordET = binding.kdbxPassword;
        ProgressDialogUtil.setLoadingProgress(alertDialog, 30);
        if (proceed) {
            if (kdbxFileUri == null) {
                ProgressDialogUtil.dismissLoadingDialog(alertDialog);
                ToastUtil.showToast(getLayoutInflater(), v, R.string.emptyFileError);
                proceed = false;
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
            TextInputEditText kdbxPasswordET = binding.kdbxPassword;
            String kdbxPassword = kdbxPasswordET.getText().toString();
            KdbxCreds creds = new KdbxCreds(kdbxPassword.getBytes());
            Common.creds = creds;
            ProgressDialogUtil.setLoadingProgress(alertDialog, 30);
            Database<?, ?, ?, ?> database = new SimpleDatabase();
            Group g = database.newGroup("Dummy Group");
            Entry e1 = database.newEntry("Dummy Entry 1");
            g.addEntry(e1);
            Entry e2 = database.newEntry("Dummy Entry 2");
            g.addEntry(e2);
            Group rootGroup = database.getRootGroup();
            rootGroup.setName("Root");
            rootGroup.addGroup(g);
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

    private void Validate() throws KpCustomException {
        TextInputEditText kdbxPasswordET = binding.kdbxPassword;
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

    private void createDummyDataFile(View v) {
        String dirPath = getFilesDir().getPath() + File.separator + "org.j_keepass";
        Log.i("JKEEPASS", "dirPath: " + dirPath);
        File projDir = new File(dirPath);
        if (!projDir.exists()) {
            projDir.mkdirs();
            Log.i("JKEEPASS", "Created dirPath: " + dirPath);
        } else {
            Log.i("JKEEPASS", "Exists dirPath: " + dirPath);
        }
        {
            dirPath = dirPath + File.separator + "data.txt";
            File f = new File(dirPath);
            if (!f.exists()) {
                try {
                    Log.i("JKEEPASS", "Try creating file dirPath: " + dirPath);
                    f.createNewFile();
                    ToastUtil.showToast(getLayoutInflater(), v, "file created");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtil.showToast(getLayoutInflater(), v, "file exists");
                Scanner myReader = null;
                try {
                    myReader = new Scanner(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String data = "data:";
                while (myReader.hasNextLine()) {
                    data = data + myReader.nextLine();

                }
                myReader.close();

                Log.i("JKEEPASS", "check Path: " + dirPath + " :: " + data);
                ToastUtil.showToast(getLayoutInflater(), v, "Path: " + dirPath + " :: " + data);
                Log.i("JKEEPASS", "data length: " + data.length());
                if (data.length() < 10) {
                    FileWriter myWriter = null;
                    try {
                        myWriter = new FileWriter(f);
                        myWriter.write("Files in Java might be tricky, but it is fun enough!");
                        myWriter.close();
                        ToastUtil.showToast(getLayoutInflater(), v, "Writing done Path: " + dirPath + " :: " + data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        ToastUtil.showToast(getLayoutInflater(), v, "Error Writing done Path: " + dirPath + " :: " + data);
                    }

                }
            }
        }

    }

    private void addDummyLayour(LinearLayout v) {
        Log.i("JKEEPASS", "get dummy layout");
        for (int i = 1; i <= 10; i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewToLoad = inflater.inflate(R.layout.activity_list_adapter_view, null);
            ((TextView) viewToLoad.findViewById(R.id.adapterText)).setText("Dummy " + i);
            v.addView(viewToLoad);
        }
    }

    private void showWelcomeMessage(View v) {
        new Thread(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                //do nothing
            }
            binding.appNameTextView.setText(R.string.welcomeBackMsg);
            binding.appNameTextView.startAnimation(AnimationUtils.loadAnimation(v.getContext(), androidx.transition.R.anim.abc_grow_fade_in_from_bottom));
        }
        ).start();

    }
}