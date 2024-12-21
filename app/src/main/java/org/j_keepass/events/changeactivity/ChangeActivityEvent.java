package org.j_keepass.events.changeactivity;

public interface ChangeActivityEvent {
    void changeActivity(ChangeActivityAction changeActivityAction);
    enum ChangeActivityAction {
        ENTRY_SELECTED,
        LOCK
    }
}
