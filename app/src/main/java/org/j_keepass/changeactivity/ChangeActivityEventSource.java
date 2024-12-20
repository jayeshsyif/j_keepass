package org.j_keepass.changeactivity;

import org.j_keepass.util.Util;

import java.util.ArrayList;

public class ChangeActivityEventSource {
    private static final ChangeActivityEventSource SOURCE = new ChangeActivityEventSource();

    private ChangeActivityEventSource() {

    }

    public static ChangeActivityEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<ChangeActivityEvent> listeners = new ArrayList<>();

    public void addListener(ChangeActivityEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(ChangeActivityEvent listener) {
        listeners.remove(listener);
    }

    public void changeActivity(ChangeActivityEvent.Action action) {
        Util.log("In listener reload");
        for (ChangeActivityEvent listener : listeners) {
            listener.changeActivity(action);
        }
    }
}
