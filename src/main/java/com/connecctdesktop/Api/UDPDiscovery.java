package com.connecctdesktop.Api;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

import com.connecctdesktop.Api.UDPDiscovery.DiscoveryResult;

public class UDPDiscovery {
    private static final int DISCOVERY_PORT = 64295;
    private static final String SHARED_SECRET = "aku suka kamu";

    public static DiscoveryResult waitForPhoneOnce() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            System.out.println("🔎 Menunggu UDP discovery dari HP di port " + DISCOVERY_PORT + "...");

            socket.receive(packet);

            String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            String ip = packet.getAddress().getHostAddress();

            System.out.println("📨 Diterima dari " + ip + ": " + msg);

            if (!msg.startsWith("CONNECCT_DISCOVERY:")) {
                System.out.println("⚠ Format pesan tidak dikenal.");
                return null;
            }

            String[] parts = msg.split(":");
            if (parts.length != 3) {
                System.out.println("⚠ Format pesan salah.");
                return null;
            }

            int httpPort = Integer.parseInt(parts[1]);
            String secret = parts[2];

            if (!SHARED_SECRET.equals(secret)) {
                System.out.println("❌ Secret salah, abaikan.");
                return null;
            }

            System.out.println("✅ HP terverifikasi. IP = " + ip + ", HTTP port = " + httpPort);
            return new DiscoveryResult(ip, httpPort);

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
}
