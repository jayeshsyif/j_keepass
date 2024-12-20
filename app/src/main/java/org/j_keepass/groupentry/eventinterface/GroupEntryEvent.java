package org.j_keepass.groupentry.eventinterface;

import org.j_keepass.fragments.entry.dtos.FieldData;

import java.util.UUID;

public interface GroupEntryEvent {
    void setGroup(UUID gId);
    void lock();
    void showAll();
    void showAllEntryOnly();
    void showAllEntryOnly(String query);
    void setEntry(UUID eId);
    void updateCacheEntry(UUID eId);
    void updateEntryField(UUID eId, FieldData fieldData);
    void updateEntry(UUID eId);
}
