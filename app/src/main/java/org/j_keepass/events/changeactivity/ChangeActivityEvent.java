package org.j_keepass.events.changeactivity;

public interface ChangeActivityEvent {
    void changeActivity(ChangeActivityAction changeActivityAction);
    enum ChangeActivityAction {
        ENTRY_SELECTED,
        ENTRY_NEW,
        LOCK,
        ENTRY_SELECTED_FOR_EDIT,
        ENTRY_DELETED,
        ENTRY_COPIED_MOVED,
    }
}
