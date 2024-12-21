package org.j_keepass.events.permission;

public interface PermissionResultEvent {
    void permissionDenied(PermissionEvent.PermissionAction permissionAction);
    void permissionGranted(PermissionEvent.PermissionAction permissionAction);
}
