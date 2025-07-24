package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DnsQuestion {
    private final String qname;
    private final RecordType qtype;
    private final RecordClass qclass;

    public DnsQuestion(String qname, RecordType qtype, RecordClass qclass) {
        this.qname = qname;
        this.qtype = qtype;
        this.qclass = qclass;

        // only supports A record querying for now
        if (this.qtype != RecordType.A) {
            throw new IllegalArgumentException("Only A record queries are allowed for now");
        }
    }

    /* @formatter:off */
    public String getQname() { return qname; }
    public RecordType getQtype() { return qtype; }
    public RecordClass getQclass() { return qclass; }
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

    public static String decodeName(ByteBuffer buf) {
        StringBuilder sb = new StringBuilder();
        int goBackPosition = -1;
        boolean jumped = false;

        while (true) {
            int len = buf.get() & 0xFF;

            if (len == 0) {
                break;
            }

            /* @formatter:off
             *
             * Realized message compression is needed:
             * https://datatracker.ietf.org/doc/html/rfc1035#section-4.1.4
             * 
             * SO the idea is ptr can be the "ending" of a domain name in a message.
             * ie, these are all allowed:
             *      "dns.google.com" === [03]dns[06]google[03]com[00]
             *      "dns.google.com" === [offset from the begining of the DNS packet to to start of '[03]dns[06]google[03]com[00]' elsewhere]
             *      "dns.google.com" === [03]dns[pointer to offset from the begining of dns packet to the start of '[06]google[03]com[00]' elsewhere]
             * 
             * After finishing reading the "dns.google.com", if pointer present, we must jump immediately after the pointer so that the next part
             * of the message can be read. The next part is the "qtype" section of the question section, i.e., DnsQuestion
             * 
             * @formatter:on
             */
            if ((len & 0b1100_0000) == 0b1100_0000) {
                int secondByte = buf.get() & 0xFF;
                int ptrOffset = ((len & 0b0011_1111) << 8) | secondByte;

                if (!jumped) {
                    // first time jumping so must remember where originating place is to jump back
                    goBackPosition = buf.position();
                    jumped = true;
                }
                buf.position(ptrOffset);
                continue;
            }

            if (len >= 64) {
                throw new IllegalArgumentException("Can't have label with size >= 64");
            }

            byte[] entry = new byte[len];
            buf.get(entry);
            // need to add the "." between "dns.google.com"
            if (!sb.isEmpty()) {
                sb.append(".");
            }
            sb.append(new String(entry, StandardCharsets.US_ASCII));
        }

        if (jumped) {
            // rewind position
            buf.position(goBackPosition);
        }

        return sb.toString();
    }


    public void writeTo(ByteBuffer buf) {
        encodeName(qname, buf);
        buf.putShort((short) qtype.getCode());
        buf.putShort((short) qclass.getCode());
    }

    public static DnsQuestion readFrom(ByteBuffer buf) {
        String name = decodeName(buf);
        RecordType qtype = RecordType.fromCode(buf.getShort() & 0xFFFF);
        RecordClass qclass = RecordClass.fromCode(buf.getShort() & 0xFFFF);
        return new DnsQuestion(name, qtype, qclass);
    }

    @Override
    public String toString() {
        return String.format("DnsQuestion{qname=%s, qtype=%s, qclass=%s}", qname, qtype, qclass);
    }



}
