package org.j_keepass.events.reload;

public interface ReloadEvent {
    void reload(ReloadEvent.ReloadAction reloadAction);
    enum ReloadAction {
        GROUP_UPDATE,
        HOME,
        CREATE_NEW,
        IMPORT,
        EDIT,
        DELETE,
        EXPORT,
    }
}
