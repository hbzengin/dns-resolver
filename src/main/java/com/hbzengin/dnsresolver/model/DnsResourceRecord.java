package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;

public class DnsResourceRecord {
    private String name;
    private int type;
    private int dataclass;
    private int ttl;
    private int rdlength;
    private byte[] rdata;

    private DnsResourceRecord() {}; // this shouldn't be called anyway so static readFrom used

    public static DnsResourceRecord readFrom(ByteBuffer buf) {
        DnsResourceRecord rr = new DnsResourceRecord();

        rr.name = DnsQuestion.decodeName(buf); // implemented earlier
        rr.type = buf.getShort() & 0xFFFF;
        rr.dataclass = buf.getShort() & 0xFFFF;
        rr.ttl = buf.getInt();
        rr.rdlength = buf.getShort() & 0xFFFF;

        rr.rdata = new byte[rr.rdlength];
        buf.get(rr.rdata);
        return rr;
    }


    /* @formatter:off
     * 
     * To understandname, type, dataclass, etc. the documentation is on the RFC
     * https://datatracker.ietf.org/doc/html/rfc1035#section-3.2.2. Some examples:
     *      Type(1) == A record
     *      Type(2) == NS record
     *      ...
     *      Class(1) = Internet (which seems to be the only practical class) 
     * 
     * @formatter:on
     */
    @Override
    public String toString() {
        StringBuilder rdataHex = new StringBuilder();
        for (byte b : rdata) {
            rdataHex.append(String.format("%02x", b & 0xFF));
        }
        return String.format(
                "ResourceRecord{name=%s, type=%d, dataclass=%d, ttl=%d, rdlength=%d, rdata=%s}",
                name, type, dataclass, ttl, rdlength, rdataHex.toString());

    }
}
