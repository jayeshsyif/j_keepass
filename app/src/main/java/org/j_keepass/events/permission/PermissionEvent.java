package org.j_keepass.events.permission;

import android.app.Activity;
import android.view.View;

public interface PermissionEvent {
    void checkAndGetPermissionReadWriteStorage(View v, Activity activity, Action action);
    void permissionDenied(Action action);
    void permissionGranted(Action action);
    void checkAndGetPermissionAlarm(View v, Activity activity, Action action);
    enum Action {
        IMPORT,
        EXPORT,
        ALARM
    }
}
