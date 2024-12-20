package org.j_keepass.changeactivity.event;

public interface ChangeActivityEvent {
    void changeActivity(Action action);
    enum Action{
        ENTRY_SELECTED,
        LOCK
    }
}
