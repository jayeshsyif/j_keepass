package org.j_keepass.permission.event;


import android.app.Activity;
import android.view.View;

import org.j_keepass.util.Util;

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

    public void checkAndGetPermissionReadWriteStorage(View v, Activity activity, PermissionEvent.Action action) {
        Util.log("In listener got Check And Get Permission, listener count is "+listeners.size());
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PermissionCheckerAndGetter.getInstance())) {
                PermissionCheckerAndGetter.register();
            }
        }
        for (PermissionEvent listener : listeners) {
            listener.checkAndGetPermissionReadWriteStorage(v, activity, action);
        }
    }

    public void permissionDenied(PermissionEvent.Action action) {
        for (PermissionEvent listener : listeners) {
            listener.permissionDenied(action);
        }
    }

    public void permissionGranted(PermissionEvent.Action action) {
        for (PermissionEvent listener : listeners) {
            listener.permissionGranted(action);
        }
    }
    public void checkAndGetPermissionAlarm(View v, Activity activity, PermissionEvent.Action action) {
        Util.log("In listener got Check And Get Permission, listener count is "+listeners.size());
        if (listeners.size() == 0 || listeners.size() > 0) {
            if (!listeners.contains(PermissionCheckerAndGetter.getInstance())) {
                PermissionCheckerAndGetter.register();
            }
        }
        for (PermissionEvent listener : listeners) {
            listener.checkAndGetPermissionAlarm(v, activity, action);
        }
    }
}
