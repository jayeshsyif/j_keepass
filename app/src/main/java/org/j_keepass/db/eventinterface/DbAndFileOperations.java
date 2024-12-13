package org.j_keepass.db.eventinterface;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.common.io.ByteStreams;

import org.j_keepass.util.Util;
import org.j_keepass.util.db.Db;
import org.j_keepass.util.db.DummyDbDataUtil;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class DbAndFileOperations {

    public String getDir(Activity activity) {
        String dirPath = "";
        if (activity != null) {
            dirPath = activity.getFilesDir().getPath() + File.separator + "org.j_keepass";
        }
        return dirPath;
    }

    public String getSubDir(Activity activity) {
        String subDirPath = "";
        if (activity != null) {
            subDirPath = activity.getFilesDir().getPath() + File.separator + "org.j_keepass" + File.separator + "kdbxfiles";
        }
        return subDirPath;
    }

    public void createMainDirectory(String dirPath) {
        Util.log("dirPath: " + dirPath);
        if (dirPath == null) {
            Util.log("Null dirPath: " + dirPath);
        } else {
            File projDir = new File(dirPath);
            if (!projDir.exists()) {
                projDir.mkdirs();
                Util.log("Created dirPath: " + dirPath);
            } else {
                Util.log("Exists dirPath: " + dirPath);
            }
        }
    }

    public void createSubFilesDirectory(String subFilesDirPath) {
        Util.log("subFilesDirPath: " + subFilesDirPath);
        if (subFilesDirPath == null) {
            Util.log("Null subFilesDirPath: " + subFilesDirPath);
        } else {
            File subFilesDir = new File(subFilesDirPath);
            if (!subFilesDir.exists()) {
                subFilesDir.mkdirs();
                Util.log("Created subFilesDirPath: " + subFilesDirPath);
            } else {
                Util.log("Exists subFilesDirPath: " + subFilesDirPath);
            }
        }
    }

    public File createFile(String dir, String dbName) {
        File fromTo = new File(dir + File.separator + dbName);
        if (fromTo.exists()) {
            fromTo.delete();
        } else {
            try {
                fromTo.createNewFile();
            } catch (Throwable e) {
                Util.log("unable to create, dir: " + dir + ", name " + dbName);
                fromTo = null;
            }
        }
        return fromTo;
    }

    public void writeDbToFile(File file, String pwd, ContentResolver contentResolver) {
        Uri kdbxFileUri = Uri.fromFile(file);
        KdbxCreds creds = new KdbxCreds(pwd.getBytes(StandardCharsets.UTF_8));
        Database<?, ?, ?, ?> database = new DummyDbDataUtil().getDummyDatabase();
        OutputStream fileOutputStream = null;
        try {
            fileOutputStream = contentResolver.openOutputStream(kdbxFileUri, "wt");
            database.save(creds, fileOutputStream);
        } catch (Throwable e) {
            Util.log("unable to write db to file");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Util.log("unable to close fos while creating db to file");
                }
            }
        }
    }

    public void importFile(String subDirPath, Uri uri, ContentResolver contentResolver, Activity activity) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.grantUriPermission(activity.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable e) {
            Util.log("unable to get permission to read file");
        }
        String fileName = null;
        try {
            Cursor returnCursor = contentResolver.query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            fileName = returnCursor.getString(nameIndex);
        } catch (Throwable e) {
            Util.log("unable to get file name");
        }
        if (fileName != null) {
            File fromTo = new File(subDirPath + File.separator + fileName);
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
                inputStream = contentResolver.openInputStream(uri);
            } catch (FileNotFoundException e) {
            }

            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(fromTo);
                ByteStreams.copy(inputStream, outputStream);
            } catch (Throwable e) {
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }


    public void openDb(String dbName, String pwd, String fullPath, ContentResolver contentResolver) {
        Util.log("Loading "+fullPath);
        KdbxCreds creds = new KdbxCreds(pwd.getBytes());
        Uri kdbxFileUri = Uri.fromFile(new File(fullPath));
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(kdbxFileUri);
        } catch (FileNotFoundException e) {
        }
        if (inputStream != null) {
            try {
                Database<?, ?, ?, ?> database = SimpleDatabase.load(creds, inputStream);
                if (database != null) {
                    Util.log("Load done");
                    Db.getInstance().setDatabase(database);
                    DbEventSource.getInstance().loadSuccessDb();
                } else {
                    DbEventSource.getInstance().failedToOpenDb("Please check password. No database found in kdbx File.");
                }
            } catch (NoClassDefFoundError e) {
                DbEventSource.getInstance().failedToOpenDb("" + e.getMessage());
            } catch (Exception e) {
                DbEventSource.getInstance().failedToOpenDb("Please check password. No database found in kdbx File.");
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
