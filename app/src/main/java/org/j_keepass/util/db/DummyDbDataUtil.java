package org.j_keepass.util.db;

import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import java.util.Calendar;

public class DummyDbDataUtil {
    public Database<?, ?, ?, ?> getDummyDatabase() {
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
