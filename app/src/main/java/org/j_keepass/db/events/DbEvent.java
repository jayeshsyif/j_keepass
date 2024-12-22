package org.j_keepass.db.events;

import android.content.Context;
import android.net.Uri;

public interface DbEvent {
    void createDb(String name, String pwd);
    void openingDb();
    void askPwdForDb(Context context, String name, String fullPath);
    void askPwdForDb(Context context, String name, Uri data);
    void failedToOpenDb(String errorMsg);
    void loadSuccessDb();
}
