package org.j_keepass.db.event;

import android.content.Context;

public interface DbEvent {
    void createDb(String name, String pwd);
    void askPwdForDb(Context context, String name, String fullPath);
    void failedToOpenDb(String errorMsg);
    void loadSuccessDb();
}
