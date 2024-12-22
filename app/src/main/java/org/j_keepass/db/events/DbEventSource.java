package org.j_keepass.db.events;


import android.content.Context;
import android.net.Uri;

import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class DbEventSource {
    private static final DbEventSource SOURCE = new DbEventSource();

    private DbEventSource() {

    }

    public static DbEventSource getInstance() {
        return SOURCE;
    }

    private final ArrayList<DbEvent> listeners = new ArrayList<>();

    public void addListener(DbEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(DbEvent listener) {
        listeners.remove(listener);
    }

    public void createDb(String dbName, String pwd) {
        Utils.log("In listener create db with name and pwd");
        for (DbEvent listener : listeners) {
            listener.createDb(dbName, pwd);
        }
    }

    public void askPwdForDb(Context context, String dbName, String fullPath) {
        Utils.log("In listener ask Pwd For Db, listener count is "+listeners.size());
        for (DbEvent listener : listeners) {
            listener.askPwdForDb(context, dbName, fullPath);
        }
    }

    public void askPwdForDb(Context context, String dbName, Uri data) {
        Utils.log("In listener ask Pwd For Db with data, listener count is "+listeners.size());
        for (DbEvent listener : listeners) {
            listener.askPwdForDb(context, dbName, data);
        }
    }

    public void failedToOpenDb(String errorMsg) {
        Utils.log("In listener failed to open db error msg is: "+errorMsg);
        for (DbEvent listener : listeners) {
            listener.failedToOpenDb(errorMsg);
        }
    }

    public void loadSuccessDb() {
        Utils.log("In listener Success to open db");
        for (DbEvent listener : listeners) {
            listener.loadSuccessDb();
        }
    }

    public void openingDb() {
        Utils.log("In listener opening to open db");
        for (DbEvent listener : listeners) {
            listener.openingDb();
        }
    }

}
