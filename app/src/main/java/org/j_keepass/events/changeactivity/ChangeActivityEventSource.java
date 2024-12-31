package org.j_keepass.events.changeactivity;

import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class ChangeActivityEventSource {
    private static final ChangeActivityEventSource SOURCE = new ChangeActivityEventSource();

    private ChangeActivityEventSource() {

    }

    public static ChangeActivityEventSource getInstance() {
        return SOURCE;
    }

    private final ArrayList<ChangeActivityEvent> listeners = new ArrayList<>();

    public void addListener(ChangeActivityEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(ChangeActivityEvent listener) {
        listeners.remove(listener);
    }

    public void changeActivity(ChangeActivityEvent.ChangeActivityAction changeActivityAction) {
        Utils.log("In listener reload");
        for (ChangeActivityEvent listener : listeners) {
            listener.changeActivity(changeActivityAction);
        }
    }
}
