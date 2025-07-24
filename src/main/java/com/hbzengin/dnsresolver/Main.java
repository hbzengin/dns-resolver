package com.hbzengin.dnsresolver;

import java.util.List;
import com.hbzengin.dnsresolver.model.*;
import com.hbzengin.dnsresolver.resolver.DnsResolver;

public class Main {
    public static void main(String[] args) {
        DnsMessage recursiveMsg = makeMessage("www.hbzengin.com");
        testRecursive(recursiveMsg);

        DnsMessage iterativeMsg = makeMessage("www.hbzengin.com");
        testIterative(iterativeMsg);
    }

    private static DnsMessage makeMessage(String hostname) {
        DnsHeader h = new DnsHeader();
        h.setId((int) (Math.random() * 65_536));
        DnsQuestion q = new DnsQuestion(hostname, RecordType.A, RecordClass.IN);
        return new DnsMessage(h, List.of(q));
    }

    private static void testRecursive(DnsMessage msg) {
        msg.getHeader().setRd(true);
        System.out.println("# Recursive");
        DnsResolver r = new DnsResolver("8.8.8.8");
        try {
            DnsMessage resp = r.resolve(msg);
            System.out.println(resp);
        } catch (Exception e) {
            System.err.println("Recursive fail: " + e.getMessage());
        }
        System.err.println("\n");
    }

    private static void testIterative(DnsMessage msg) {
        msg.getHeader().setRd(false);
        System.out.println("# Iterative ");
        DnsResolver r = new DnsResolver("198.41.0.4");
        try {
            DnsMessage resp = r.resolve(msg);
            System.out.println(resp);
        } catch (Exception e) {
            System.err.println("Iterative fail " + e.getMessage());
        }
        System.err.println("\n");
    }
}
