package org.j_keepass.events.permission;

import android.app.Activity;
import android.view.View;

public interface PermissionEvent {
    void checkAndGetPermissionReadWriteStorage(View v, Activity activity, PermissionAction permissionAction);
    void checkAndGetPermissionAlarm(View v, Activity activity, PermissionAction permissionAction);
    enum PermissionAction {
        IMPORT,
        EXPORT,
        ALARM,
        SHARE
    }
}
