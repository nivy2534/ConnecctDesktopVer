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

    private String getAvailableLocalIp() {
    try {
        java.util.Enumeration<java.net.NetworkInterface> interfaces = 
            java.net.NetworkInterface.getNetworkInterfaces();
        
        java.util.List<String> wifiIps = new java.util.ArrayList<>();
        java.util.List<String> vpnIps = new java.util.ArrayList<>();
        java.util.List<String> otherIps = new java.util.ArrayList<>();
        
        while (interfaces.hasMoreElements()) {
            java.net.NetworkInterface iface = interfaces.nextElement();
            if (!iface.isUp() || iface.isLoopback()) continue;
            
            String ifaceName = iface.getName().toLowerCase();
            
            java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                java.net.InetAddress addr = addresses.nextElement();
                if (!(addr instanceof java.net.Inet4Address)) continue;
                
                String ipAddr = addr.getHostAddress();
                
                if (ifaceName.contains("wlan") || ifaceName.contains("eth") || 
                    ifaceName.contains("en") || ifaceName.contains("wifi") ||
                    ifaceName.contains("ethernet")) {
                    wifiIps.add(ipAddr);
                }
                else if (ifaceName.contains("tun") || ifaceName.contains("ppp") || 
                         ifaceName.contains("tap") || ifaceName.contains("vpn") ||
                         ifaceName.contains("openVPN") || ifaceName.contains("wireguard")) {
                    vpnIps.add(ipAddr);
                }
                else {
                    otherIps.add(ipAddr);
                }
            }
        }
        
        if (!wifiIps.isEmpty()) {
            return wifiIps.get(0);
        } else if (!vpnIps.isEmpty()) {
            return vpnIps.get(0);
        } else if (!otherIps.isEmpty()) {
            return otherIps.get(0);
        }
        
    } catch (Exception e) {
    }
    return "127.0.0.1";
    }

    private String getUsername() {
        return System.getProperty("user.name");
    }
}