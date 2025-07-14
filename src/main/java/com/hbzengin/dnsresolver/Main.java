package com.hbzengin.dnsresolver;

import java.nio.ByteBuffer;
import java.util.List;
import com.hbzengin.dnsresolver.model.DnsHeader;
import com.hbzengin.dnsresolver.model.DnsMessage;
import com.hbzengin.dnsresolver.model.DnsQuestion;
import com.hbzengin.dnsresolver.resolver.DnsResolver;

public class Main {
    public static void main(String[] args) {
        DnsHeader header = new DnsHeader();
        header.setId(100);

        /* Asking a recursive DNS server to find it for us */
        header.setRd(true);

        DnsQuestion q = new DnsQuestion("dns.google.com", 1, 1);
        DnsMessage msg = new DnsMessage(header, List.of(q));

        DnsResolver resolver = new DnsResolver("8.8.8.8", 53);
        try {
            byte[] byteResponse = resolver.sendQuery(msg);
            DnsMessage response = DnsMessage.fromByteBuffer(ByteBuffer.wrap(byteResponse));
            System.out.println(response);

        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
        }
    }
}
