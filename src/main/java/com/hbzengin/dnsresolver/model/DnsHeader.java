package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;

// Header as per RFC 1035 https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.1
public class DnsHeader {
    private int id; // 16 bits used in header
    private boolean qr;
    private int opcode; // 4 bits used in header
    private boolean aa;
    private boolean tc;
    private boolean rd;
    private boolean ra;
    private int z; // unused (reserved for future use)
    private int rcode; // 4 bits used in header

    // counts
    private int qdcount; // 16 bits used in header
    private int ancount; // 16 bits used in header
    private int nscount; // 16 bits used in header
    private int arcount; // 16 bits used in header

    // default ctor no work done here
    public DnsHeader() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id & 0xFFFF;
    }

    public boolean isQr() {
        return qr;
    }

    public void setQr(boolean qr) {
        this.qr = qr;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public boolean isAa() {
        return aa;
    }

    public void setAa(boolean aa) {
        this.aa = aa;
    }

    public boolean isTc() {
        return aa;
    }

    public void setTc(boolean tc) {
        this.tc = tc;
    }

    public boolean isRd() {
        return rd;
    }

    public void setRd(boolean rd) {
        this.rd = rd;
    }

    public boolean isRa() {
        return ra;
    }

    public void setRa(boolean ra) {
        this.ra = ra;
    }

    public void writeTo(ByteBuffer buf) {
    }

    public static DnsHeader readFrom(ByteBuffer buf) {

    }

    @Override
    public String toString() {
        return String.format(
                "DnsHeader{id=%d, qr=%b, opcode=%d, aa=%b, tc=%b, rd=%b, ra=%b, z=%d, rcode=%d, qdcount=%d, ancount=%d, nscount=%d, arcount=%d}",
                id, qr, opcode, aa, tc, rd, ra, z, rcode, qdcount, ancount, nscount, arcount);
    }

}
