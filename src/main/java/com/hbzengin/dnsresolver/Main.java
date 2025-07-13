package com.hbzengin.dnsresolver;

import java.util.List;
import com.hbzengin.dnsresolver.model.DnsHeader;
import com.hbzengin.dnsresolver.model.DnsMessage;
import com.hbzengin.dnsresolver.model.DnsQuestion;

public class Main {
    public static void main(String[] args) {
        DnsHeader header = new DnsHeader();
        header.setId(22);
        header.setRd(true);
        DnsQuestion q = new DnsQuestion("dns.google.com", 1, 1);
        DnsMessage msg = new DnsMessage(header, List.of(q));
        msg.printHex();
    }
}
