package com.asset.VMS.domain;

import java.io.Serializable;

public class VesselDataType implements Serializable {
    private static final long serialVersionUID = 1L;

    private int dataTypeId;
    private String dataTypeDesc;
    private String dataTypeRegex;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(int dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public String getDataTypeDesc() {
        return dataTypeDesc;
    }

    public void setDataTypeDesc(String dataTypeDesc) {
        this.dataTypeDesc = dataTypeDesc;
    }

    public String getDataTypeRegex() {
        return dataTypeRegex;
    }

    public void setDataTypeRegex(String dataTypeRegex) {
        this.dataTypeRegex = dataTypeRegex;
    }
}
