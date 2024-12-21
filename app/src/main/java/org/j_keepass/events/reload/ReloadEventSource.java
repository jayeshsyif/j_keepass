package org.j_keepass.events.reload;

import org.j_keepass.events.interfaces.ReloadAction;
import org.j_keepass.util.Utils;

import java.util.ArrayList;

public class ReloadEventSource {
    private static final ReloadEventSource SOURCE = new ReloadEventSource();

    private ReloadEventSource() {

    }

    public static ReloadEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<ReloadEvent> listeners = new ArrayList<>();

    public void addListener(ReloadEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(ReloadEvent listener) {
        listeners.remove(listener);
    }

    public void reload(ReloadAction action) {
        Utils.log("In listener reload");
        for (ReloadEvent listener : listeners) {
            listener.reload(action);
        }
    }
}
