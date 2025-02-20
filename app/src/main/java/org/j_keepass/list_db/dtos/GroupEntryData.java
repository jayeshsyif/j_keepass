package org.j_keepass.list_db.dtos;

import java.util.UUID;

public class GroupEntryData {
    public UUID id;
    public String name, path;
    public GroupEntryType type;
    public int subCount;
    public GroupEntryStatus status;
    public long daysToExpire;
}