package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DnsQuestion {
    private final String qname;
    private final int qtype;
    private final int qclass;

    public DnsQuestion(String qname, int qtype, int qclass) {
        this.qname = qname;
        this.qtype = qtype & 0xFFFF;
        this.qclass = qclass & 0xFFFF;
    }

    /* @formatter:off */
    public String getQname() { return qname; }
    public int getQtype() { return qtype; }
    public int getQclass() { return qclass; }
    /* @formatter:on */


    // eg: dns.google.com will be encoded as 3dns6google3com0
    private static void encodeName(String name, ByteBuffer buf) throws IllegalArgumentException {
        for (String label : name.split("\\.", -1)) {
            // need ASCII for protocol, this uses utf-8 normally
            byte[] bytes = label.getBytes(StandardCharsets.US_ASCII);
            if (bytes.length >= 64) {
                throw new IllegalArgumentException("Can't have label with size >= 64");
            }
            buf.put((byte) bytes.length);
            buf.put(bytes);
        }
        buf.put((byte) 0); // ends with 0
    }

    private static String decodeName(ByteBuffer buf) {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int len = buf.get() & 0xFF;

            if (len == 0) {
                break;
            }
            if (len >= 64) {
                throw new IllegalArgumentException("Can't have label with size >= 64");
            }

            byte[] entry = new byte[len];
            buf.get(entry);

            if (!sb.isEmpty()) {
                sb.append(".");
            }
            sb.append(new String(entry, StandardCharsets.US_ASCII));
        }

        return sb.toString();
    }


    public void writeTo(ByteBuffer buf) {
        encodeName(qname, buf);
        buf.putShort((short) qtype);
        buf.putShort((short) qclass);
    }

    public static DnsQuestion readFrom(ByteBuffer buf) {
        String name = decodeName(buf);
        int qtype = buf.getShort() & 0xFFFF;
        int qclass = buf.getShort() & 0xFFFF;
        return new DnsQuestion(name, qtype, qclass);
    }

    @Override
    public String toString() {
        return String.format("DnsQuestion{qname=%s, qtype=%d, qclass=%d}", qname, qtype, qclass);
    }



}
