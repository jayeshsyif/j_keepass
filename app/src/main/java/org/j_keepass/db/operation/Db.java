package org.j_keepass.db.operation;

import android.content.ContentResolver;

import org.j_keepass.db.events.DbAndFileOperations;
import org.j_keepass.fields.dtos.FieldData;
import org.j_keepass.fields.enums.FieldNameType;
import org.j_keepass.fields.enums.FieldValueType;
import org.j_keepass.list_db.dtos.GroupEntryData;
import org.j_keepass.list_db.dtos.GroupEntryStatus;
import org.j_keepass.list_db.dtos.GroupEntryType;
import org.j_keepass.util.Pair;
import org.j_keepass.util.Utils;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.Group;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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

    private String appDirPath, appSubDir;

    private Database<?, ?, ?, ?> database;

    private UUID currentGroupId = null;

    private UUID currentEntryId = null;
    private ArrayList<UUID> listOfEntriesNotUpdatedInDb = new ArrayList<>();

    private byte[] pwd = null;
    private File kdbxFile = null;

    public void setDatabase(Database<?, ?, ?, ?> database, File kdbxFile, byte[] pwd) {
        this.database = database;
        this.kdbxFile = kdbxFile;
        this.pwd = pwd;
        currentGroupId = database.getRootGroup().getUuid();
    }

    public String getDbName() {
        if (database != null) {
            return database.getName();
        } else {
            return "Not Found!";
        }
    }

    public String getAppDirPath() {
        return appDirPath;
    }

    public void setAppDirPath(String appDirPath) {
        this.appDirPath = appDirPath;
    }

    public String getAppSubDir() {
        return appSubDir;
    }

    public void setAppSubDir(String appSubDir) {
        this.appSubDir = appSubDir;
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
            if (group != null && group.getParent() != null) {
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

    public String getEntryTitle(UUID gId) {
        String title = "";
        if (database != null) {
            Entry<?, ?, ?, ?> entry = database.findEntry(gId);
            if (entry != null) {
                title = entry.getTitle();
            }
        }
        return title;
    }

    public long getSubGroupsCount(UUID gId) {
        long gCount = 0;
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                gCount = group.getGroupsCount();
            }
        }
        return gCount;
    }

    public long getSubEntriesCount(UUID gId) {
        long gCount = 0;
        if (database != null) {
            Group<?, ?, ?, ?> group = database.findGroup(gId);
            if (group != null) {
                gCount = group.getEntriesCount();
            }
        }
        return gCount;
    }

    public long getAllEntriesCount() {
        long eCount = 0;
        if (database != null) {
            List<?> entries = database.findEntries(entry -> true);
            if (entries != null) {
                eCount = entries.size();
            }
        }
        return eCount;
    }

    public long getAllEntriesCount(String query) {
        long eCount = 0;
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
        List<?> entries = database.findEntries(entry -> true);
        if (entries != null) {
            for (int eCount = 0; eCount < entries.size(); eCount++) {
                Entry<?, ?, ?, ?> entry = (Entry<?, ?, ?, ?>) entries.get(eCount);
                GroupEntryData data = new GroupEntryData();
                data.id = entry.getUuid();
                data.name = entry.getTitle();
                data.type = GroupEntryType.ENTRY;
                data.path = entry.getPath();
                Pair<GroupEntryStatus, Long> statusLongPair = getStatus(entry.getExpiryTime());
                data.status = statusLongPair.first;
                data.daysToExpire = statusLongPair.second;
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
                data.path = entry.getPath();
                Pair<GroupEntryStatus, Long> statusLongPair = getStatus(entry.getExpiryTime());
                data.status = statusLongPair.first;
                data.daysToExpire = statusLongPair.second;
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
                data.path = suEntry.getPath();
                Pair<GroupEntryStatus, Long> statusLongPair = getStatus(suEntry.getExpiryTime());
                data.status = statusLongPair.first;
                data.daysToExpire = statusLongPair.second;
                list.add(data);
            }
        }
        return list;
    }

    public Pair<GroupEntryStatus, Long> getStatus(Date date) {
        GroupEntryStatus status;
        Pair<GroupEntryStatus, Long> statusLongPair = new Pair<>();
        statusLongPair.first = null;
        statusLongPair.second = 0L;
        Date currentDate = Calendar.getInstance().getTime();
        long diff = date.getTime() - currentDate.getTime();
        long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        statusLongPair.second = daysToExpire;
        if (daysToExpire <= 0) {
            status = GroupEntryStatus.EXPIRED;
        } else if (daysToExpire <= 10) {
            status = GroupEntryStatus.EXPIRING_SOON;
        } else {
            status = GroupEntryStatus.OK;
        }
        statusLongPair.first = status;
        return statusLongPair;
    }

    public long getAllExpiredEntriesCount() {
        Date currentDate = Calendar.getInstance().getTime();
        long eCount = 0;
        if (database != null) {
            List<?> entries = database.findEntries(entry -> {
                long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
                long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                return daysToExpire <= 0;
            });
            if (entries != null) {
                eCount = entries.size();
            }
        }
        return eCount;
    }

    public long getAllExpiringSoonEntriesCount() {
        Date currentDate = Calendar.getInstance().getTime();
        long eCount = 0;
        if (database != null) {
            List<?> entries = database.findEntries(entry -> {
                long diff = entry.getExpiryTime().getTime() - currentDate.getTime();
                long daysToExpire = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                return (daysToExpire > 0 && daysToExpire <= 10);
            });
            if (entries != null) {
                eCount = entries.size();
            }
        }
        return eCount;
    }

    public void deSetDatabase() {
        database = null;
        pwd = null;
        kdbxFile = null;
        currentGroupId = null;
        currentEntryId = null;
        listOfEntriesNotUpdatedInDb = new ArrayList<>();
    }

    public UUID getCurrentGroupId() {
        if (currentGroupId == null) {
            currentGroupId = getRootGroupId();
        }
        return currentGroupId;
    }

    public ArrayList<FieldData> getFields(UUID eId) {
        ArrayList<FieldData> fields = new ArrayList<>();
        if (database != null) {
            Entry<?, ?, ?, ?> entry = database.findEntry(eId);
            fields = getFields(entry);
        }
        return fields;
    }

    private ArrayList<FieldData> getFields(Entry<?, ?, ?, ?> entry) {
        ArrayList<FieldData> fields = new ArrayList<>();
        if (entry != null) {
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.TITLE.toString();
                fd.fieldNameType = FieldNameType.TITLE;
                fd.value = entry.getTitle();
                fd.fieldValueType = FieldValueType.TEXT;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.USERNAME.toString();
                fd.value = entry.getUsername();
                fd.fieldNameType = FieldNameType.USERNAME;
                fd.fieldValueType = FieldValueType.TEXT;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.PASSWORD.toString();
                fd.value = entry.getPassword();
                fd.fieldValueType = FieldValueType.PASSWORD;
                fd.fieldNameType = FieldNameType.PASSWORD;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.URL.toString();
                fd.fieldNameType = FieldNameType.URL;
                fd.value = entry.getUrl();
                fd.fieldValueType = FieldValueType.URL;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.NOTES.toString();
                fd.fieldNameType = FieldNameType.NOTES;
                fd.value = entry.getNotes();
                fd.fieldValueType = FieldValueType.LARGE_TEXT;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.CREATED_DATE.toString();
                fd.value = Utils.convertDateToString(entry.getCreationTime());
                fd.fieldValueType = FieldValueType.TEXT;
                fd.fieldNameType = FieldNameType.CREATED_DATE;
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.EXPIRY_DATE.toString();
                fd.value = Utils.convertDateToString(entry.getExpiryTime());
                fd.fieldValueType = FieldValueType.TEXT;
                fd.fieldNameType = FieldNameType.EXPIRY_DATE;
                fd.expiryDate = entry.getExpiryTime();
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.LAST_ACCESSED.toString();
                fd.value = Utils.convertDateToString(entry.getLastAccessTime());
                fd.fieldValueType = FieldValueType.TEXT;
                fd.fieldNameType = FieldNameType.DATE;
                fd.expiryDate = entry.getExpiryTime();
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
            {
                FieldData fd = new FieldData();
                fd.eId = entry.getUuid();
                fd.name = FieldNameType.LAST_MODIFIED.toString();
                fd.value = Utils.convertDateToString(entry.getLastModificationTime());
                fd.fieldValueType = FieldValueType.TEXT;
                fd.fieldNameType = FieldNameType.DATE;
                fd.expiryDate = entry.getExpiryTime();
                if (fd.value != null && fd.value.length() > 0) {
                    fields.add(fd);
                }
            }
        }
        return fields;
    }

    public void setCurrentGroupId(UUID currentGroupId) {
        this.currentGroupId = currentGroupId;
    }

    public UUID getCurrentEntryId() {
        return currentEntryId;
    }

    public void setCurrentEntryId(UUID currentEntryId) {
        this.currentEntryId = currentEntryId;
    }

    public ArrayList<FieldData> getAdditionalFields(Entry<?, ?, ?, ?> entry) {
        ArrayList<String> ignoreFields = new ArrayList<>() {{
            add("username".toLowerCase());
            add("password".toLowerCase());
            add("url".toLowerCase());
            add("title".toLowerCase());
            add("notes".toLowerCase());
        }};
        ArrayList<FieldData> fields = new ArrayList<>();
        if (entry != null) {
            if (entry.getPropertyNames().size() > 0) {
                for (String pn : entry.getPropertyNames()) {
                    Utils.log("Additional property found " + pn);
                    if (!ignoreFields.contains(pn.toLowerCase())) {
                        FieldData fd = new FieldData();
                        fd.eId = entry.getUuid();
                        fd.name = pn;
                        fd.value = entry.getProperty(pn);
                        fd.fieldNameType = FieldNameType.ADDITIONAL;
                        fd.fieldValueType = FieldValueType.TEXT;
                        if (fd.value != null && fd.value.length() > 0) {
                            Utils.log("Additional property added " + pn);
                            fields.add(fd);
                        }
                    }
                }
            }
        }
        return fields;
    }

    public ArrayList<FieldData> getAdditionalFields(UUID eId) {
        ArrayList<FieldData> fields = new ArrayList<>();
        if (database != null) {
            Entry<?, ?, ?, ?> entry = database.findEntry(eId);
            fields = getAdditionalFields(entry);
        }
        return fields;
    }

    public ArrayList<FieldData> getAttachments(UUID eId) {
        ArrayList<FieldData> fields = new ArrayList<>();
        if (database != null) {
            Entry<?, ?, ?, ?> entry = database.findEntry(eId);
            if (entry != null) {
                if (entry.getBinaryPropertyNames().size() > 0) {
                    for (String bn : entry.getBinaryPropertyNames()) {
                        Utils.log("binary property found " + bn);
                        FieldData fd = new FieldData();
                        fd.name = FieldNameType.ATTACHMENT.toString();
                        fd.value = bn;
                        fd.fieldNameType = FieldNameType.ATTACHMENT;
                        fd.fieldValueType = FieldValueType.ATTACHMENT;
                        if (fd.value != null && fd.value.length() > 0) {
                            fields.add(fd);
                        }
                    }
                }
            }
        }
        return fields;
    }


    public boolean updateEntryField(UUID eId, FieldData fieldData) {
        boolean isUpdated = false;
        if (database != null) {
            Entry<?, ?, ?, ?> entry = database.findEntry(eId);
            if (entry != null) {
                updateEntryField(entry, fieldData);
                isUpdated = true;
            } else {
                Utils.log("entry is null");
            }
        }
        return isUpdated;
    }

    private boolean updateEntryField(Entry<?, ?, ?, ?> entry, FieldData fieldData) {
        Utils.log("Update actual field data " + fieldData.asString());
        boolean isUpdated = false;
        if (database != null) {
            if (entry != null) {
                if (fieldData.fieldNameType.name().equals(FieldNameType.USERNAME.name())) {
                    entry.setUsername(fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.PASSWORD.name())) {
                    entry.setPassword(fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.NOTES.name())) {
                    entry.setNotes(fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.URL.name())) {
                    entry.setUrl(fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.TITLE.name())) {
                    entry.setTitle(fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.EXPIRY_DATE.name())) {
                    entry.setExpiryTime(Utils.convertStringToDate(fieldData.value));
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.ADDITIONAL.name())) {
                    entry.setProperty(fieldData.name, fieldData.value);
                } else if (fieldData.fieldNameType.name().equals(FieldNameType.ATTACHMENT.name())) {
                }
                isUpdated = true;
            }
        }
        return isUpdated;
    }

    public void updateCacheEntry(UUID eId) {
        if (!listOfEntriesNotUpdatedInDb.contains(eId)) {
            listOfEntriesNotUpdatedInDb.add(eId);
        }
    }

    public boolean isEntryNotUpdatedInDb(UUID eId) {
        return listOfEntriesNotUpdatedInDb.contains(eId);
    }

    public void updateDb(ContentResolver contentResolver) {
        new DbAndFileOperations().writeDbToFile(kdbxFile, pwd, contentResolver, database);
    }

    public void updateDb(ContentResolver contentResolver, byte[] newPwd) {
        new DbAndFileOperations().writeDbToFile(kdbxFile, newPwd, contentResolver, database);
        pwd = newPwd;
    }

    public void updateEntry(UUID eId) {
        if (listOfEntriesNotUpdatedInDb.contains(eId)) {
            listOfEntriesNotUpdatedInDb.remove(eId);
        }
    }

    public boolean pwdMatch(String newPwd) {
        return new String(pwd).equals(newPwd);
    }
}
