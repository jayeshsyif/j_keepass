package org.j_keepass.db.eventinterface;

public interface DbEvent {
    void createDb(String name, String pwd);
    void reloadDbFile();
}
