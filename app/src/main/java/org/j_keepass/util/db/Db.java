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
import java.util.List;
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

    private UUID currentGroupId = null;

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

    public int getAllEntriesCount() {
        int eCount = 0;
        if (database != null) {
            List<?> entries = database.findEntries(entry -> true);
            if (entries != null) {
                eCount = entries.size();
            }
        }
        return eCount;
    }
    public int getAllEntriesCount(String query) {
        int eCount = 0;
        if (database != null) {
            List<?> entries = database.findEntries(entry -> entry.match(query));
            if (entries != null) {
                eCount = entries.size();
            }
        }
        return eCount;
    }

    public ArrayList<GroupEntryData> getAllEntries() {
        ArrayList<GroupEntryData> list = new ArrayList<>();
        Date currentDate = Calendar.getInstance().getTime();
        List<?> entries = database.findEntries(entry -> true);
        if (entries != null) {
            for (int eCount = 0; eCount < entries.size(); eCount++) {
                Entry<?, ?, ?, ?> entry = (Entry<?, ?, ?, ?>) entries.get(eCount);
                GroupEntryData data = new GroupEntryData();
                data.id = entry.getUuid();
                data.name = entry.getTitle();
                data.type = GroupEntryType.ENTRY;
                long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
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
        return list;
    }
    public ArrayList<GroupEntryData> getAllEntries(String query) {
        ArrayList<GroupEntryData> list = new ArrayList<>();
        Date currentDate = Calendar.getInstance().getTime();
        List<?> entries = database.findEntries(entry -> entry.match(query));
        if (entries != null) {
            for (int eCount = 0; eCount < entries.size(); eCount++) {
                Entry<?, ?, ?, ?> entry = (Entry<?, ?, ?, ?>) entries.get(eCount);
                GroupEntryData data = new GroupEntryData();
                data.id = entry.getUuid();
                data.name = entry.getTitle();
                data.type = GroupEntryType.ENTRY;
                long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
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
        return list;
    }

    public ArrayList<GroupEntryData> getSubGroupsAndEntries(UUID gId) {
        ArrayList<GroupEntryData> list = new ArrayList<>();
        Date currentDate = Calendar.getInstance().getTime();
        Group<?, ?, ?, ?> group = database.findGroup(gId);
        if (group != null) {
            for (Group<?, ?, ?, ?> suGroup : group.getGroups()) {
                GroupEntryData data = new GroupEntryData();
                data.id = suGroup.getUuid();
                data.name = suGroup.getName();
                data.type = GroupEntryType.GROUP;
                data.subCount = suGroup.getGroupsCount() + suGroup.getEntriesCount();
                list.add(data);
            }
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
        return list;
    }

    public void deSetDatabase() {
        database = null;
    }

    public UUID getCurrentGroupId() {
        return currentGroupId;
    }

    public void setCurrentGroupId(UUID currentGroupId) {
        this.currentGroupId = currentGroupId;
    }
}
