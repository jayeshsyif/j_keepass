package org.j_keepass.db.events;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.google.common.io.ByteStreams;

import org.j_keepass.db.operation.Db;
import org.j_keepass.list_group_and_entry.activities.ListGroupAndEntriesActivity;
import org.j_keepass.util.Utils;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        Utils.log("dirPath: " + dirPath);
        if (dirPath == null) {
            Utils.log("Null dirPath: " + dirPath);
        } else {
            File projDir = new File(dirPath);
            if (!projDir.exists()) {
                projDir.mkdirs();
                Utils.log("Created dirPath: " + dirPath);
            } else {
                Utils.log("Exists dirPath: " + dirPath);
            }
        }
    }

    public void createSubFilesDirectory(String subFilesDirPath) {
        Utils.log("subFilesDirPath: " + subFilesDirPath);
        if (subFilesDirPath == null) {
            Utils.log("Null subFilesDirPath: " + subFilesDirPath);
        } else {
            File subFilesDir = new File(subFilesDirPath);
            if (!subFilesDir.exists()) {
                subFilesDir.mkdirs();
                Utils.log("Created subFilesDirPath: " + subFilesDirPath);
            } else {
                Utils.log("Exists subFilesDirPath: " + subFilesDirPath);
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
                Utils.log("unable to create, dir: " + dir + ", name " + dbName);
                fromTo = null;
            }
        }
        return fromTo;
    }

    public void writeDbToFile(File file, byte[] pwd, ContentResolver contentResolver, Database<?, ?, ?, ?> database) {
        Utils.log("write db to file");
        Uri kdbxFileUri = Uri.fromFile(file);
        KdbxCreds creds = new KdbxCreds(pwd);
        OutputStream fileOutputStream = null;
        try {
            fileOutputStream = contentResolver.openOutputStream(kdbxFileUri, "wt");
            database.save(creds, fileOutputStream);
        } catch (Throwable e) {
            Utils.log("unable to write db to file");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    Utils.log("unable to close fos while creating db to file");
                }
            }
        }
    }

    public void importFile(String subDirPath, Uri uri, ContentResolver contentResolver, Activity activity) {
        boolean proceed = true;
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.grantUriPermission(activity.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable e) {
            proceed = false;
            Utils.log("unable to get permission to read file");
        }
        if (proceed) {
            String fileName = null;
            try {
                Cursor returnCursor = contentResolver.query(uri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                fileName = returnCursor.getString(nameIndex);
            } catch (Throwable e) {
                Utils.log("unable to get file name");
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
                        // ignore
                    }
                }

                InputStream inputStream = null;
                try {
                    inputStream = contentResolver.openInputStream(uri);
                } catch (FileNotFoundException e) {
                    // ignore
                }

                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(fromTo);
                    ByteStreams.copy(inputStream, outputStream);
                } catch (Throwable e) {
                    // ignore
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }


    public void openDb(String dbName, String pwd, String fullPath, ContentResolver contentResolver) {
        Utils.log("Loading " + fullPath);
        KdbxCreds creds = new KdbxCreds(pwd.getBytes());
        File kdbxFile = new File(fullPath);
        Uri kdbxFileUri = Uri.fromFile(kdbxFile);
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(kdbxFileUri);
        } catch (FileNotFoundException e) {
            // ignore
        }
        if (inputStream != null) {
            try {
                Database<?, ?, ?, ?> database = SimpleDatabase.load(creds, inputStream);
                if (database != null) {
                    Utils.log("Load done");
                    Db.getInstance().setDatabase(database, kdbxFile, pwd.getBytes());
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
                    // ignore
                }
            }
        }
    }

    public void exportFile(Database<?, ?, ?, ?> database, Uri uri, byte[] pwd, ContentResolver contentResolver, ListGroupAndEntriesActivity activity) {
        boolean proceed = true;
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.grantUriPermission(activity.getPackageName(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable e) {
            proceed = false;
            Utils.log("unable to get permission to read file");
        }
        if (proceed) {
            OutputStream fileOutputStream = null;
            KdbxCreds creds = new KdbxCreds(pwd);
            try {
                fileOutputStream = activity.getContentResolver().openOutputStream(uri, "wt");
                database.save(creds, fileOutputStream);
            } catch (Throwable t) {
                Utils.log("unable to export file");
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {
                        //do nothing
                    }
                }
            }
        }
    }
}
