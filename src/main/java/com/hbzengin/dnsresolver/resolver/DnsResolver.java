package com.hbzengin.dnsresolver.resolver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import com.hbzengin.dnsresolver.model.DnsHeader;
import com.hbzengin.dnsresolver.model.DnsMessage;

public class DnsResolver {
    private final InetSocketAddress serverAddr;


    public DnsResolver(String serverIp, int port) {
        this.serverAddr = new InetSocketAddress(serverIp, port);
    }

    private byte[] sendQuery(DnsMessage query) throws Exception {
        ByteBuffer buf = query.toByteBuffer();
        byte[] out = new byte[buf.remaining()];
        buf.get(out);

        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setSoTimeout(10_000);
            DatagramPacket req = new DatagramPacket(out, out.length, serverAddr);
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
            byte[] rawResponse = sendQuery(query);
            return DnsMessage.fromByteBuffer(ByteBuffer.wrap(rawResponse));
        }

        // iterative case: we handle instead of some recursive DNS server
        while (true) {
            System.out.printf("Querying %s for %s \n", serverAddr, );
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
