package org.j_keepass.fields.dtos;

import org.j_keepass.fields.enums.FieldNameType;
import org.j_keepass.fields.enums.FieldValueType;

import java.util.Date;
import java.util.UUID;

public class FieldData {
    public UUID eId;
    public String name;
    public String value;
    public byte[] fileInBytes;
    public FieldNameType fieldNameType;
    public FieldValueType fieldValueType;
    public Date expiryDate;

    public String asString() {
        return "FieldData{" +
                "id=" + eId +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", fieldNameType=" + fieldNameType +
                ", fieldValueType=" + fieldValueType +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
