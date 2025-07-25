package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class DnsMessage {
    private DnsHeader header;
    private List<DnsQuestion> questions;
    private List<DnsResourceRecord> answers;
    private List<DnsResourceRecord> authorities;
    private List<DnsResourceRecord> additionals;


    // for creating a request
    public DnsMessage(DnsHeader header, List<DnsQuestion> questions) {
        this.header = header;
        this.questions = questions;

        // empty init for toString to not have null errors
        this.answers = new ArrayList<>();
        this.authorities = new ArrayList<>();
        this.additionals = new ArrayList<>();

        header.setQdcount(questions.size());

    }

    // @formatter:off
    public DnsHeader getHeader() { return header; }
    public List<DnsQuestion> getQuestions() { return questions; }
    public List<DnsResourceRecord> getAnswers() { return answers; }
    public List<DnsResourceRecord> getAuthorities() { return authorities; }
    public List<DnsResourceRecord> getAdditionals() { return additionals; }
    // @formatter:on

    public ByteBuffer toByteBuffer() {
        // DNS's UDP messages required to be 512 bytes
        // https://datatracker.ietf.org/doc/html/rfc1035#section-2.3.4
        // must be big endian (==network byte order)
        ByteBuffer buf = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN);
        header.writeTo(buf);
        for (DnsQuestion q : questions) {
            q.writeTo(buf);
        }
        buf.flip();
        return buf;
    }

    private static List<DnsResourceRecord> readResourceRecords(ByteBuffer buf, int num) {
        List<DnsResourceRecord> list = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            list.add(DnsResourceRecord.readFrom(buf));
        }
        return list;
    }

    // actually returns a response because also considers an, ns, ar
    public static DnsMessage fromByteBuffer(ByteBuffer buf) {
        buf.order(ByteOrder.BIG_ENDIAN);
        DnsHeader hdr = DnsHeader.readFrom(buf);

        List<DnsQuestion> qs = new ArrayList<>();
        for (int i = 0; i < hdr.getQdcount(); i++) {
            qs.add(DnsQuestion.readFrom(buf));
        }

        List<DnsResourceRecord> an = readResourceRecords(buf, hdr.getAncount());
        List<DnsResourceRecord> ns = readResourceRecords(buf, hdr.getNscount());
        List<DnsResourceRecord> ar = readResourceRecords(buf, hdr.getArcount());

        DnsMessage msg = new DnsMessage(hdr, qs);
        msg.answers = an;
        msg.authorities = ns;
        msg.additionals = ar;
        return msg;
    }


    public void printHex() {
        ByteBuffer buf = this.toByteBuffer();
        StringBuilder sb = new StringBuilder();
        while (buf.hasRemaining()) {
            sb.append(String.format("%02x", buf.get()));
        }
        System.out.println(sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-------------\t[DNS Message]\t-------------\n");
        sb.append("Header: ").append(header).append('\n');

        sb.append("\nQuestions:\n");
        for (DnsQuestion q : questions) {
            sb.append("  ").append(q).append('\n');
        }

        sb.append("\nAnswers:\n");
        for (DnsResourceRecord rr : answers) {
            sb.append("  ").append(rr).append('\n');
        }

        sb.append("\nAuthorities:\n");
        for (DnsResourceRecord rr : authorities) {
            sb.append("  ").append(rr).append('\n');
        }

        sb.append("\nAdditionals:\n");
        for (DnsResourceRecord rr : additionals) {
            sb.append("  ").append(rr).append('\n');
        }
        return sb.toString();
    }
}
