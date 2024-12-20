package org.j_keepass.changeactivity;

public interface ChangeActivityEvent {
    void changeActivity(Action action);
    enum Action{
        CHANGE,
        LOCK
    }
}
