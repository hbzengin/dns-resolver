package com.hbzengin.dnsresolver.model;

public enum RecordType {
    A(1), NS(2), CNAME(5), UNKNOWN(-1); // only A, NS supported by my resolver currently

    private final int code;

    RecordType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RecordType fromCode(int code) {
        for (RecordType rt : values()) {
            if (rt.code == code) {
                return rt;
            }
        }
        return UNKNOWN;
    }
}
