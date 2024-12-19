package org.j_keepass.groupentry.eventinterface;

import java.util.UUID;

public interface GroupEntryEvent {
    void setGroup(UUID gId);
    void lock();
    void showAll();
    void showAllEntryOnly();
    void showAllEntryOnly(String query);
    void setEntry(UUID gId);
}
