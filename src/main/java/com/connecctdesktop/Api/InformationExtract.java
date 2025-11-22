package com.connecctdesktop.Api;

import java.util.*;
import java.net.*;

public class InformationExtract {
    public InformationExtract() {

    }

    public List<String> getAvailableIp() {
        List<String> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (!iface.isUp() || iface.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> address = iface.getInetAddresses();

                while (address.hasMoreElements()) {
                    InetAddress adr = address.nextElement();

                    // ✔ Hanya IPv4
                    if (adr instanceof Inet4Address) {

                        // ✔ Skip loopback (127.x.x.x)
                        if (!adr.isLoopbackAddress()) {
                            ips.add(adr.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ips;
    }

    public String getLocalIp() {
        List<String> ips = getAvailableIp();
        if (!ips.isEmpty()) {
            // Return IP pertama yang bukan loopback
            for (String ip : ips) {
                if (!ip.startsWith("127")) {
                    return ip;
                }
            }
        }
        return "localhost";
    }

    public String getUsername() {
        return System.getProperty("user.name");
    }
}