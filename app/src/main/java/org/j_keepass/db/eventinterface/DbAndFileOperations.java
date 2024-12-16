package org.j_keepass.db.eventinterface;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import org.j_keepass.util.Util;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

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
        Database<?, ?, ?, ?> database = getDummyDatabase();
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
}
