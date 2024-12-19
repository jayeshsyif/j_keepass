package org.j_keepass.fragments.entry.dtos;

import java.util.Date;
import java.util.UUID;

public class FieldData {
    public UUID id;
    public String name;
    public String value;
    public FieldNameType fieldNameType;
    public FieldValueType fieldValueType;
    public Date expiryDate;
}
