package org.j_keepass.db.eventinterface;


import android.content.Context;

import org.j_keepass.util.Util;

import java.util.ArrayList;

public class DbEventSource {
    private static final DbEventSource SOURCE = new DbEventSource();

    private DbEventSource() {

    }

    public static DbEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<DbEvent> listeners = new ArrayList<>();

    public void addListener(DbEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(DbEvent listener) {
        listeners.remove(listener);
    }

    public void createDb(String dbName, String pwd) {
        Util.log("In listener create db with name and pwd");
        for (DbEvent listener : listeners) {
            listener.createDb(dbName, pwd);
        }
    }

    public void askPwdForDb(Context context, String dbName, String fullPath) {
        Util.log("In listener ask Pwd For Db, listener count is "+listeners.size());
        for (DbEvent listener : listeners) {
            listener.askPwdForDb(context, dbName, fullPath);
        }
    }

    public void failedToOpenDb(String errorMsg) {
        Util.log("In listener failed to open db");
        for (DbEvent listener : listeners) {
            listener.failedToOpenDb(errorMsg);
        }
    }

    public void loadSuccessDb() {
        Util.log("In listener failed to open db");
        for (DbEvent listener : listeners) {
            listener.loadSuccessDb();
        }
    }

}
