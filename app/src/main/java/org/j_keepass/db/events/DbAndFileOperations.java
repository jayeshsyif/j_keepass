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

import java.io.ByteArrayOutputStream;
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
            Utils.log("Null dirPath: ");
        } else {
            File projDir = new File(dirPath);
            if (!projDir.exists()) {
                boolean isDone = projDir.mkdirs();
                if (isDone) {
                    Utils.log("Created dirPath: " + dirPath);
                } else {
                    Utils.log("Not Created dirPath: " + dirPath);
                }
            } else {
                Utils.log("Exists dirPath: " + dirPath);
            }
        }
    }

    public void createSubFilesDirectory(String subFilesDirPath) {
        Utils.log("subFilesDirPath: " + subFilesDirPath);
        if (subFilesDirPath == null) {
            Utils.log("Null subFilesDirPath: ");
        } else {
            File subFilesDir = new File(subFilesDirPath);
            if (!subFilesDir.exists()) {
                boolean isDone = subFilesDir.mkdirs();
                if (isDone) {
                    Utils.log("Created subFilesDirPath: " + subFilesDirPath);
                } else {
                    Utils.log("Not Created subFilesDirPath: " + subFilesDirPath);
                }
            } else {
                Utils.log("Exists subFilesDirPath: " + subFilesDirPath);
            }
        }
    }

    public File createFile(String dir, String dbName) {
        File fromTo = new File(dir + File.separator + dbName);
        if (fromTo.exists()) {
            boolean isDone = fromTo.delete();
            if (isDone) {
                Utils.log("Deleted dir: " + dir + ", name " + dbName);
            }
        } else {
            try {
                boolean isDone = fromTo.createNewFile();
                if (!isDone) {
                    Utils.log("unable to create, dir: " + dir + ", name " + dbName);
                }
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
            try (Cursor returnCursor = contentResolver.query(uri, null, null, null, null)) {
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                }
            } catch (Throwable e) {
                Utils.log("unable to get file name");
            }
            if (fileName != null) {
                File fromTo = new File(subDirPath + File.separator + fileName);
                if (fromTo.exists()) {
                    boolean isDone = fromTo.delete();
                    if (!isDone) {
                        Utils.log("unable to delete");
                    }
                } else {
                    try {
                        boolean isDone;
                        isDone = fromTo.createNewFile();
                        if (!isDone) {
                            Utils.log("unable to create new file");
                        }
                        isDone = fromTo.setWritable(true, true);
                        if (!isDone) {
                            Utils.log("unable to set writable");
                        }
                        isDone = fromTo.setExecutable(true, true);
                        if (!isDone) {
                            Utils.log("unable to set executable");
                        }
                        isDone = fromTo.setReadable(true, true);
                        if (!isDone) {
                            Utils.log("unable to set readable");
                        }
                    } catch (IOException e) {
                        Utils.ignoreError(e);
                    }
                }

                InputStream inputStream = null;
                try {
                    inputStream = contentResolver.openInputStream(uri);
                } catch (FileNotFoundException e) {
                    Utils.ignoreError(e);
                }
                FileOutputStream outputStream = null;
                try {
                    if (inputStream != null) {
                        outputStream = new FileOutputStream(fromTo);
                        ByteStreams.copy(inputStream, outputStream);
                    }
                } catch (Throwable e) {
                    Utils.ignoreError(e);
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Utils.ignoreError(e);
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            Utils.ignoreError(e);
                        }
                    }
                }
            }
        }
    }


    public void openDb(String dbName, String pwd, String fullPath, ContentResolver contentResolver) {
        Utils.log("Loading " + fullPath + " with dbname as " + dbName);
        DbEventSource.getInstance().openingDb();
        KdbxCreds creds = new KdbxCreds(pwd.getBytes());
        File kdbxFile = new File(fullPath);
        Uri kdbxFileUri = Uri.fromFile(kdbxFile);
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(kdbxFileUri);
        } catch (FileNotFoundException e) {
            DbEventSource.getInstance().failedToOpenDb("File not found.");
        }
        if (inputStream != null) {
            try {
                Database<?, ?, ?, ?> database = SimpleDatabase.load(creds, inputStream);
                Utils.log("Load done");
                Db.getInstance().setDatabase(database, kdbxFile, pwd.getBytes());
                DbEventSource.getInstance().loadSuccessDb();
            } catch (NoClassDefFoundError e) {
                DbEventSource.getInstance().failedToOpenDb("" + e.getMessage());
            } catch (Exception e) {
                DbEventSource.getInstance().failedToOpenDb("Please check password. No database found in kdbx File.");
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Utils.ignoreError(e);
                }
            }
        }
    }

    public void openDb(String dbName, String pwd, Uri kdbxFileUri, ContentResolver contentResolver) {
        Utils.log("Loading " + dbName);
        DbEventSource.getInstance().openingDb();
        KdbxCreds creds = new KdbxCreds(pwd.getBytes());
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(kdbxFileUri);
        } catch (FileNotFoundException e) {
            DbEventSource.getInstance().failedToOpenDb("File not found.");
        }
        if (inputStream != null) {
            try {
                Database<?, ?, ?, ?> database = SimpleDatabase.load(creds, inputStream);
                Utils.log("Load done");
                if (kdbxFileUri.getPath() != null) {
                    Db.getInstance().setDatabase(database, new File(kdbxFileUri.getPath()), pwd.getBytes());
                    DbEventSource.getInstance().loadSuccessDb();
                } else {
                    DbEventSource.getInstance().failedToOpenDb(" Path is null");
                }
            } catch (NoClassDefFoundError e) {
                DbEventSource.getInstance().failedToOpenDb("" + e.getMessage());
            } catch (Exception e) {
                DbEventSource.getInstance().failedToOpenDb("Please check password. No database found in kdbx File.");
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    Utils.ignoreError(e);
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
            KdbxCreds creds = new KdbxCreds(pwd);
            try (OutputStream fileOutputStream = activity.getContentResolver().openOutputStream(uri, "wt")) {
                database.save(creds, fileOutputStream);
            } catch (Throwable t) {
                Utils.log("unable to export file");
            }
            //do nothing
        }
    }

    public byte[] getFileIntoBytes(Uri dataUri, ContentResolver contentResolver, Activity activity) {
        byte[] value = new byte[0];
        boolean proceed = true;
        try {
            contentResolver.takePersistableUriPermission(dataUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.grantUriPermission(activity.getPackageName(), dataUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable e) {
            proceed = false;
            Utils.log("unable to get permission to read file");
        }
        if (proceed) {
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(dataUri);
            } catch (FileNotFoundException e) {
                Utils.ignoreError(e);
            }
            if (inputStream != null) {
                try {
                    value = toByteArray(inputStream);
                } catch (IOException e) {
                    Utils.ignoreError(e);
                }
            }
        }
        return value;
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        return outputStream.toByteArray();
    }

    public String getFileName(Uri dataUri, ContentResolver contentResolver, Activity activity) {
        String fileName = "";
        boolean proceed = true;
        try {
            contentResolver.takePersistableUriPermission(dataUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.grantUriPermission(activity.getPackageName(), dataUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable e) {
            proceed = false;
            Utils.log("unable to get permission to read file");
        }
        if (proceed) {
            try (Cursor returnCursor = contentResolver.query(dataUri, null, null, null, null)) {
                if (returnCursor != null) {
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                }
            } catch (Throwable e) {
                Utils.log("unable to get file name");
            }
        }
        return fileName;
    }
}
