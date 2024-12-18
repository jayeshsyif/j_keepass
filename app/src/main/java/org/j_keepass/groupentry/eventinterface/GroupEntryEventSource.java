package org.j_keepass.groupentry.eventinterface;


import org.j_keepass.util.Util;

import java.util.ArrayList;
import java.util.UUID;

public class GroupEntryEventSource {

    private static final GroupEntryEventSource SOURCE = new GroupEntryEventSource();

    private GroupEntryEventSource() {

    }
    public static GroupEntryEventSource getInstance() {
        return SOURCE;
    }

    private ArrayList<GroupEntryEvent> listeners = new ArrayList<>();

    public void addListener(GroupEntryEvent listener) {
        listeners.add(listener);
    }

    public void removeListener(GroupEntryEvent listener) {
        listeners.remove(listener);
    }

    public void setGroup(UUID uuid) {
        Util.log("In listener set group");
        for (GroupEntryEvent listener : listeners) {
            listener.setGroup(uuid);
        }
    }

    public void lock() {
        Util.log("In listener lock");
        for (GroupEntryEvent listener : listeners) {
            listener.lock();
        }
    }
}
