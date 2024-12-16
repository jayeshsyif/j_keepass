package org.j_keepass.db.eventinterface;


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

    public void reloadDbFile() {
        Util.log("In listener reload Db File");
        for (DbEvent listener : listeners) {
            listener.reloadDbFile();
        }
    }
}
