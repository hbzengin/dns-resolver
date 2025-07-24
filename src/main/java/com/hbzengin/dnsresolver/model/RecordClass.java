package com.hbzengin.dnsresolver.model;

public enum RecordClass {
    IN(1), UNKNOWN(-1); // only two supported by my resolver currently

    private final int code;

    RecordClass(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RecordClass fromCode(int code) {
        for (RecordClass rc : values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        return UNKNOWN;
    }


}
