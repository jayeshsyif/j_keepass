package org.j_keepass.fragments.listdatabase.dtos;

import java.util.UUID;

public class GroupEntryData {
    public UUID id;
    public String name;
    public GroupEntryType type;
    public int subCount;
    public GroupEntryStatus status;
    public long daysToExpire;
}