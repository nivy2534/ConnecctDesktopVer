package com.connecctdesktop.Api;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.connecctdesktop.Api.UDPDiscovery.DiscoveryResult;

public class UDPDiscovery {
    private static final int DISCOVERY_PORT = 33220;
    private static final String prefix = "CONNECCT_DISCOVERY:";

    public static DiscoveryResult waitForPhoneOnce(String localSecret) {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            System.out.println("🔎 Menunggu UDP discovery dari HP di port " + DISCOVERY_PORT + "...");

            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            String ip = packet.getAddress().getHostAddress();

            System.out.println("📨 Diterima dari " + ip + ": " + msg);

            if (!msg.startsWith(prefix)) {
                System.out.println("⚠ Format pesan tidak dikenal.");
                return null;
            }

            String receivedSecret = msg.substring(prefix.length()).trim();

            System.out.println("📌 Secret diterima: '" + receivedSecret + "'");
            System.out.println("📌 Secret lokal  : '" + localSecret + "'");

            if (!checkSecret(receivedSecret, localSecret)) {
                System.out.println("❌ Secret tidak cocok.");
                return null;
            }

            byte[] reply = "OK".getBytes(StandardCharsets.UTF_8);
            DatagramPacket resp = new DatagramPacket(
                    reply,
                    reply.length,
                    packet.getAddress(),
                    packet.getPort());
            socket.send(resp);
            System.out.println("✅ Balasan OK dikirim ke " + ip + ":" + packet.getPort());

            // Kalau kamu pengen simpan sesuatu:
            return new DiscoveryResult(ip, /* httpPort */ 80);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class DiscoveryResult {
        public final String ip;
        public final int httpPort;

        public DiscoveryResult(String ip, int httpPort) {
            this.ip = ip;
            this.httpPort = httpPort;
        }
    }

    public static Boolean checkSecret(String secret, String localSecret) {
        if (secret == null || localSecret == null)
            return false;
        return MessageDigest.isEqual(secret.getBytes(StandardCharsets.UTF_8),
                localSecret.getBytes(StandardCharsets.UTF_8));
    }
}
