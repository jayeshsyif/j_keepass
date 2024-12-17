package org.j_keepass.permission.eventinterface;

import android.app.Activity;
import android.view.View;

public interface PermissionEvent {
    void checkAndGetPermission(View v, Activity activity, Action action);
    void permissionDenied(Action action);
    void permissionGranted(Action action);
    enum Action {
        IMPORT,
        EXPORT
    }
}
