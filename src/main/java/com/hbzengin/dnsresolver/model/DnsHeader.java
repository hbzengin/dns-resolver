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
    // private int z; // not used, reserved for later
    private int rcode; // 4 bits used in header

    // counts
    private int qdcount; // 16 bits used in header
    private int ancount; // 16 bits used in header
    private int nscount; // 16 bits used in header
    private int arcount; // 16 bits used in header

    /* @formatter:off */
    public DnsHeader() { } // default ctor, no work done here
    public int getId() { return id; }
    public void setId(int id) { this.id = id & 0xFFFF; }
    public boolean isQr() { return qr; } 
    public void setQr(boolean qr) { this.qr = qr; }
    public int getOpcode() { return opcode; }
    public void setOpcode(int opcode) { this.opcode = opcode & 0xF; }
    public boolean isAa() { return aa; }
    public void setAa(boolean aa) { this.aa = aa; }
    public boolean isTc() { return tc; }
    public void setTc(boolean tc) { this.tc = tc; }
    public boolean isRd() { return rd; }
    public void setRd(boolean rd) { this.rd = rd; }
    public boolean isRa() { return ra; }
    public void setRa(boolean ra) { this.ra = ra; }
    public int getRcode() { return rcode; }
    public void setRcode(int rcode) { this.rcode = rcode & 0xF; }
    public int getQdcount() { return qdcount; }
    public void setQdcount(int qdcount) { this.qdcount = qdcount & 0xFFFF; }
    public int getAncount() { return ancount; }
    public void setAncount(int ancount) { this.ancount = ancount & 0xFFFF; }
    public int getNscount() { return nscount; }
    public void setNscount(int nscount) { this.nscount = nscount & 0xFFFF; }
    public int getArcount() { return arcount; }
    public void setArcount(int rcount) { this.arcount = rcount & 0xFFFF; }
    /* @formatter:on */

    public void writeTo(ByteBuffer buf) {
        buf.putShort((short) id);

        int row = 0;
        row |= ((qr ? 1 : 0) << 15);
        row |= opcode << 11;
        row |= (aa ? 1 : 0) << 10;
        row |= (tc ? 1 : 0) << 9;
        row |= (rd ? 1 : 0) << 8;
        row |= (ra ? 1 : 0) << 7;
        row |= rcode;
        buf.putShort((short) row);

        buf.putShort((short) qdcount);
        buf.putShort((short) ancount);
        buf.putShort((short) nscount);
        buf.putShort((short) arcount);
    }

    public static DnsHeader readFrom(ByteBuffer buf) {
        DnsHeader h = new DnsHeader();
        h.setId(buf.getShort());

        int row = buf.getShort() & 0xFFFF;
        h.setQr((row & (1 << 15)) != 0);
        h.setOpcode((row >>> 11) & 0xF);
        h.setAa((row & (1 << 10)) != 0);
        h.setTc((row & (1 << 9)) != 0);
        h.setRd((row & (1 << 8)) != 0);
        h.setRa((row & (1 << 7)) != 0);
        h.setRcode(row & 0xF);

        h.setQdcount(buf.getShort());
        h.setAncount(buf.getShort());
        h.setNscount(buf.getShort());
        h.setArcount(buf.getShort());

        return h;
    }

    @Override
    public String toString() {
        return String.format(
                "DnsHeader{id=%d, qr=%b, opcode=%d, aa=%b, tc=%b, rd=%b, ra=%b, rcode=%d, qdcount=%d, ancount=%d, nscount=%d, arcount=%d}",
                id, qr, opcode, aa, tc, rd, ra, rcode, qdcount, ancount, nscount, arcount);
    }

}
