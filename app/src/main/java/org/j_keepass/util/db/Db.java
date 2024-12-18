package org.j_keepass.util.db;

import org.j_keepass.fragments.listdatabase.dtos.GroupEntryData;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryStatus;
import org.j_keepass.fragments.listdatabase.dtos.GroupEntryType;
import org.j_keepass.fragments.listgroupentry.Action;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Db {
    private static final Db DB = new Db();

    private Db() {
    }

    public static Db getInstance() {
        return DB;
    }

    private Database<?, ?, ?, ?> database;

    public void setDatabase(Database<?, ?, ?, ?> database) {
        this.database = database;
    }

    public String getDbName() {
        if (database != null) {
            return database.getName();
        } else {
            return "Not Found!";
        }
    }

    public String getRootGroupName() {
        if (database != null) {
            return database.getRootGroup().getName();
        } else {
            return "Not Found!";
        }
    }

    public UUID getRootGroupId() {
        if (database != null) {
            return database.getRootGroup().getUuid();
        } else {
            return null;
        }
    }

    public UUID getParentGroupId(UUID gId) {
        UUID pGid = null;
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                pGid = group.getParent().getUuid();
            }
        }
        return pGid;
    }

    public String getGroupName(UUID gId) {
        String name = "";
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                name = group.getName();
            }
        }
        return name;
    }

    public int getSubGroupsCount(UUID gId) {
        int gCount = 0;
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                gCount = group.getGroupsCount();
            }
        }
        return gCount;
    }

    public int getSubEntriesCount(UUID gId) {
        int gCount = 0;
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                gCount = group.getEntriesCount();
            }
        }
        return gCount;
    }

    public ArrayList<GroupEntryData> getSubGroupsAndEntries(UUID gId, Action action) {
        ArrayList<GroupEntryData> list = new ArrayList<>();
        Group<?, ?, ?, ?> group = database.findGroup(gId);
        if (group != null) {
            if (action.name().equals(Action.ALL.name()) || action.name().equals(Action.GROUP_ONLY.name())) {
                for (Group<?, ?, ?, ?> suGroup : group.getGroups()) {
                    GroupEntryData data = new GroupEntryData();
                    data.id = suGroup.getUuid();
                    data.name = suGroup.getName();
                    data.type = GroupEntryType.GROUP;
                    data.subCount = suGroup.getGroupsCount() + suGroup.getEntriesCount();
                    list.add(data);
                }
            }
            if (action.name().equals(Action.ALL.name()) || action.name().equals(Action.ENTRY_ONLY.name())) {
                Date currentDate = Calendar.getInstance().getTime();
                for (Entry<?, ?, ?, ?> suEntry : group.getEntries()) {
                    GroupEntryData data = new GroupEntryData();
                    data.id = suEntry.getUuid();
                    data.name = suEntry.getTitle();
                    data.type = GroupEntryType.ENTRY;
                    long diff = suEntry.getExpiryTime().getTime() - currentDate.getTime();
                    long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    data.daysToExpire = daysToExpire;
                    if (daysToExpire <= 0) {
                        data.status = GroupEntryStatus.EXPIRED;
                    } else if (daysToExpire > 0 && daysToExpire <= 10) {
                        data.status = GroupEntryStatus.EXPIRNG_SOON;
                    } else {
                        data.status = GroupEntryStatus.OK;
                    }
                    list.add(data);
                }
            }
        }
        return list;
    }

    public void deSetDatabase() {
        database = null;
    }
}
