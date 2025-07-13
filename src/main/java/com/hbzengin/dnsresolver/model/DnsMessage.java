package com.hbzengin.dnsresolver.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class DnsMessage {
    private DnsHeader header;
    private List<DnsQuestion> questions;

    public DnsMessage(DnsHeader header, List<DnsQuestion> questions) {
        this.header = header;
        this.questions = questions;
        header.setQdcount(questions.size());

    }

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


    public void printHex() {
        ByteBuffer buf = this.toByteBuffer();
        StringBuilder sb = new StringBuilder();
        while (buf.hasRemaining()) {
            sb.append(String.format("%02x", buf.get()));
        }
        System.out.println(sb.toString());
    }
}
