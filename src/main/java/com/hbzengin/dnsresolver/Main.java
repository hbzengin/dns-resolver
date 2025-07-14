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
        header.setRd(false);

        // for dns.google.com, with type=1 (A record) and class=1 (Internet)
        // https://datatracker.ietf.org/doc/html/rfc1035#section-3.2.4
        DnsQuestion q = new DnsQuestion("dns.google.com", 1, 1);
        DnsMessage msg = new DnsMessage(header, List.of(q));

        /*
         * use if recursion is on, this is Google's recursive DNS server DnsResolver resolver = new
         * DnsResolver("8.8.8.8", 53);
         */

        /*
         * use if recursion is off, this is an authoritative root server w/o recursion
         * https://en.wikipedia.org/wiki/Root_name_server. Below is a Verisign root server
         */
        DnsResolver resolver = new DnsResolver("198.41.0.4", 53);
        

        try {
            byte[] byteResponse = resolver.sendQuery(msg);
            DnsMessage response = DnsMessage.fromByteBuffer(ByteBuffer.wrap(byteResponse));
            System.out.println(response);

        } catch (Exception e) {
            System.err.println("Error " + e.getMessage());
        }
    }
}
