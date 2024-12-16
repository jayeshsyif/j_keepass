package org.j_keepass.db.eventinterface;

import android.content.Context;

public interface DbEvent {
    void createDb(String name, String pwd);
    void reloadDbFile();
    void askPwdForDb(Context context, String name, String fullPath);
    void failedToOpenDb(String errorMsg);
    void loadSuccessDb();
}
