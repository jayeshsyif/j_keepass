package org.j_keepass.events.permission;


import android.app.Activity;
import android.view.View;

import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class PermissionEventSource {
    private static final PermissionEventSource SOURCE = new PermissionEventSource();

    private PermissionEventSource() {
    }

    public static PermissionEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<PermissionEvent> listeners = new ArrayList<>();

    public void addListener(PermissionEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(PermissionEvent listener) {
        listeners.remove(listener);
    }

    public void checkAndGetPermissionReadWriteStorage(View v, Activity activity, PermissionEvent.PermissionAction permissionAction) {
        Utils.log("In listener got Check And Get Permission, listener count is "+listeners.size());
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PermissionCheckerAndGetter.getInstance())) {
                PermissionCheckerAndGetter.register();
            }
        }
        for (PermissionEvent listener : listeners) {
            listener.checkAndGetPermissionReadWriteStorage(v, activity, permissionAction);
        }
    }

    public void permissionDenied(PermissionEvent.PermissionAction permissionAction) {
        for (PermissionEvent listener : listeners) {
            listener.permissionDenied(permissionAction);
        }
    }

    public void permissionGranted(PermissionEvent.PermissionAction permissionAction) {
        for (PermissionEvent listener : listeners) {
            listener.permissionGranted(permissionAction);
        }
    }
    public void checkAndGetPermissionAlarm(View v, Activity activity, PermissionEvent.PermissionAction permissionAction) {
        Utils.log("In listener got Check And Get Permission, listener count is "+listeners.size());
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PermissionCheckerAndGetter.getInstance())) {
                PermissionCheckerAndGetter.register();
            }
        }
        for (PermissionEvent listener : listeners) {
            listener.checkAndGetPermissionAlarm(v, activity, permissionAction);
        }
    }
}
