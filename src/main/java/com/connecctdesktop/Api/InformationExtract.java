package com.connecctdesktop.Api;

import java.util.*;
import java.net.*;

public class InformationExtract {
    InformationExtract() {

    }

    private List<String> getAvailableIp() {
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
                    ips.add(adr.getHostAddress());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ips;
    }

    private String getUsername() {
        return System.getProperty("user.name");
    }
}