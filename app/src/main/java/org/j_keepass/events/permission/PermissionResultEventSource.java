package org.j_keepass.events.permission;


import java.util.ArrayList;

public class PermissionResultEventSource {
    private static final PermissionResultEventSource SOURCE = new PermissionResultEventSource();

    private PermissionResultEventSource() {
    }

    public static PermissionResultEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<PermissionResultEvent> listeners = new ArrayList<>();

    public void addListener(PermissionResultEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(PermissionResultEvent listener) {
        listeners.remove(listener);
    }


    public void permissionDenied(PermissionEvent.PermissionAction permissionAction) {
        for (PermissionResultEvent listener : listeners) {
            listener.permissionDenied(permissionAction);
        }
    }

    public void permissionGranted(PermissionEvent.PermissionAction permissionAction) {
        for (PermissionResultEvent listener : listeners) {
            listener.permissionGranted(permissionAction);
        }
    }
}
