package org.j_keepass.util.db;

import org.linguafranca.pwdb.Database;

public class Db {
    private static final Db DB = new Db();

    private Db() {
    }

    public static Db getInstance() {
        return DB;
    }

    private Database<?, ?, ?, ?> database;

    public void setDatabase(Database<?, ?, ?, ?> database) {
        this.database = database;
    }

    public String getRootGroupName() {
        if (database != null) {
            return database.getName();
        } else {
            return "Not Found!";
        }
    }
}
