package com.hbzengin.dnsresolver.resolver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.hbzengin.dnsresolver.model.*;

public class DnsResolver {
    private final InetSocketAddress serverAddr;
    private static final int DNS_PORT = 53;

    // Fallback if iterative 'fails' at any point
    private static final DnsResolver GOOGLE_RECURSIVE = new DnsResolver("8.8.8.8", DNS_PORT);

    public DnsResolver(String serverIp, int port) {
        this.serverAddr = new InetSocketAddress(serverIp, port);
    }

    // CTOR without port specified, defaults to DNS_PORT=53
    public DnsResolver(String serverIp) {
        this.serverAddr = new InetSocketAddress(serverIp, DNS_PORT);
    }

    private byte[] sendQuery(DnsMessage query, InetSocketAddress dest) throws Exception {
        ByteBuffer buf = query.toByteBuffer();
        byte[] out = new byte[buf.remaining()];
        buf.get(out);

        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setSoTimeout(2_000);
            DatagramPacket req = new DatagramPacket(out, out.length, dest);
            sock.send(req);

            byte[] resp_buf = new byte[512]; // can be max 512
            DatagramPacket resp = new DatagramPacket(resp_buf, resp_buf.length);
            sock.receive(resp);

            return Arrays.copyOf(resp.getData(), resp.getLength());
        }
    }

    public DnsMessage resolve(DnsMessage query) throws Exception {
        DnsHeader h = query.getHeader();

        // recursive case is simple
        if (h.isRd()) {
            byte[] rawResponse = sendQuery(query, serverAddr);
            return DnsMessage.fromByteBuffer(ByteBuffer.wrap(rawResponse));
        }

        // iterative case: we handle instead of some recursive DNS server
        // 'serverAddr' must be a non-recursive DNS server
        InetSocketAddress current = serverAddr;

        while (true) {
            System.out.printf("Querying %s for %s \n",
                    current.getAddress().getHostAddress(),
                    // I read most implementations send 1 Question, so I will do the same
                    // and here I will get it with .get(0) here.
                    query.getQuestions().get(0));

            byte[] byteResp = sendQuery(query, current);
            DnsMessage resp = DnsMessage.fromByteBuffer(ByteBuffer.wrap(byteResp));

            for (DnsResourceRecord rr : resp.getAnswers()) {
                // if any of the answers is an "A" record, we are good!
                if (rr.getType() == RecordType.A) {
                    return resp;
                }

                if (rr.getType() == RecordType.CNAME) {
                    throw new Exception("CName responses currently not supported");
                }
            }


            InetSocketAddress next = null;

            // if no A in answers, then must go through NS responses in authorities
            List<String> nsNames = new ArrayList<>();
            for (DnsResourceRecord rr : resp.getAuthorities()) {
                if (rr.getType() == RecordType.NS) {
                    nsNames.add(rr.getDecodedRdata());
                }
            }

            for (DnsResourceRecord rr : resp.getAdditiionals()) {
                // if A record exists and the A record is for one of the nameservers received
                if (rr.getType() == RecordType.A && nsNames.contains(rr.getName())) {
                    // per spec rr.RData should be IPv4 address
                    InetAddress ip = InetAddress.getByAddress(rr.getRdata());
                    next = new InetSocketAddress(ip, serverAddr.getPort());
                    break;
                }
            }

            if (next == null) {
                for (String ns : nsNames) {
                    System.out.println("Does this actually ever happen IN PRACTICE?");
                    DnsHeader h2 = new DnsHeader();
                    h2.setId(h.getId());
                    // make this recursive so that someone else can recursively find for us
                    h2.setRd(true);
                    h2.setOpcode(0);

                    DnsQuestion q = new DnsQuestion(ns, RecordType.A, RecordClass.IN);
                    DnsMessage req = new DnsMessage(h2, List.of(q));
                    DnsMessage res = GOOGLE_RECURSIVE.resolve(req);

                    for (DnsResourceRecord rr : res.getAnswers()) {
                        if (rr.getType() == RecordType.A) {
                            InetAddress ip = InetAddress.getByAddress(rr.getRdata());
                            next = new InetSocketAddress(ip, serverAddr.getPort());
                            break;
                        }
                    }

                    // early termination
                    if (next != null) {
                        break;
                    }
                }
            }

            // if still null, no NS found to continue
            if (next == null) {
                throw new Exception("No NS found to continue. Is this even possible?");
            }

            // for next iteration
            current = next;

        }

    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
