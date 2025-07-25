package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;


public class DnsResourceRecord {
    private String name;
    private RecordType type;
    private RecordClass recordClass; // class keyword taken
    private int ttl;
    private int rdlength;
    private byte[] rdata;

    private String decodedRdata;

    private DnsResourceRecord() {}; // this shouldn't be called anyway so static readFrom used

    public static DnsResourceRecord readFrom(ByteBuffer buf) {
        DnsResourceRecord rr = new DnsResourceRecord();

        rr.name = DnsQuestion.decodeName(buf); // important to account for compression logic
        rr.type = RecordType.fromCode(buf.getShort() & 0xFFFF);
        rr.recordClass = RecordClass.fromCode(buf.getShort() & 0xFFFF);
        rr.ttl = buf.getInt();
        rr.rdlength = buf.getShort() & 0xFFFF;

        rr.rdata = new byte[rr.rdlength];
        buf.get(rr.rdata);

        if (rr.type == RecordType.NS || rr.type == RecordType.CNAME) {
            // if NS (nameserver), we need the domain name of the NS in string format
            // and NOT raw bytes. So this is convenient to have for later when matching
            buf.position(buf.position() - rr.rdlength);
            rr.decodedRdata = DnsQuestion.decodeName(buf);
        }

        return rr;
    }

    /* @formatter:off */
    public String getName() { return name; }
    public RecordType getType() { return type; }
    public RecordClass getRecordClass() { return recordClass; }
    public int getTtl() { return ttl; }
    public int getRdlength() { return rdlength; }
    public byte[] getRdata() { return rdata; }
    public String getDecodedRdata() { return decodedRdata; }
    /* @formatter:on */


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
        String rdataStr;

        if (type == RecordType.A && rdata.length == 4) {
            rdataStr = String.format("%d.%d.%d.%d",
                    rdata[0] & 0xFF,
                    rdata[1] & 0xFF,
                    rdata[2] & 0xFF,
                    rdata[3] & 0xFF);
        } else {
            StringBuilder sb = new StringBuilder();
            for (byte b : rdata) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            rdataStr = sb.toString();

        }
        return String.format(
                "ResourceRecord{name=%s, type=%s, class=%s, ttl=%d, rdlength=%d, rdata=%s}",
                name, type, recordClass, ttl, rdlength, rdataStr);

    }
}
