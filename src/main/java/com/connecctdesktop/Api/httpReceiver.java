package com.connecctdesktop.Api;

import com.sun.javafx.application.HostServicesDelegate;
import com.sun.net.httpserver.*;
import com.google.gson.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;

public class httpReceiver {
    private HttpServer server;
    private final int port;
    private final String pairToken;

    public httpReceiver(int port, String pairToken) {
        this.port = port;
        this.pairToken = pairToken;
    }

    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/info", new InfoHandler());
        server.createContext("/keys/import", new ImportKeyHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("HTTP receiver running on port " + port);
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("HTTP server stopped");
        }
    }

    class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendPlain(exchange, 405, "Method Not Allowed");
                    return;
                }
                if (!checkPairToken(exchange.getRequestHeaders())) {
                    sendPlain(exchange, 401, "Unauthorized");
                    return;
                }

                String os = System.getProperty("os.name");
                String user = System.getProperty("user.name");
                String deviceName = resolveHostname();

                JsonObject json = new JsonObject();
                json.addProperty("os", os);
                json.addProperty("user", user);
                json.addProperty("deviceName", deviceName);
            } catch (Exception e) {
                e.printStackTrace();
                sendPlain(exchange, 500, "Internal Server Error");
            }
        }
    }

    class ImportKeyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendPlain(exchange, 405, "Method Not Allowed");
                    return;
                }

                if (!checkPairToken(exchange.getRequestHeaders())) {
                    sendPlain(exchange, 401, "Unauthorized");
                    return;
                }

                String body = readBody(exchange.getRequestBody());
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                String publicKey = json.has("public_key") ? json.get("public_key").getAsString().trim() : null;
                String comment = json.has("comment") ? json.get("comment").getAsString().trim() : null;

                if (publicKey == null || publicKey.isEmpty()) {
                    sendPlain(exchange, 400, "public_key is required");
                    return;
                }

                boolean ok = appendToAuthKeys(publicKey, comment);

                if (ok) {
                    JsonObject resp = new JsonObject();
                    resp.addProperty("status", "ok");
                    sendJson(exchange, 200, resp.toString());
                } else {
                    sendPlain(exchange, 500, "Failed to save key");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendPlain(exchange, 500, "Internal Server Error");
            }
        }
    }

    private boolean checkPairToken(Headers headers) {
        if (pairToken == null || pairToken.isBlank()) {
            return false;
        }

        String expected = UDPDiscovery.lastHttpToken != null ? UDPDiscovery.lastHttpToken : pairToken;

        List<String> values = headers.get("X-Pair-Token");
        if (values == null || values.isEmpty())
            return false;
        String received = values.get(0);
        String expectedClean = expected.trim();
        String receivedClean = received.trim();

        System.out.println("X-Pair-Token received: '" + receivedClean + "'");
        System.out.println("X-Pair-Token expected: '" + expectedClean + "'");

        return receivedClean.equals(expectedClean);
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            String envHost = System.getenv("COMPUTERNAME");
            if (envHost != null && !envHost.isBlank())
                return envHost;
            envHost = System.getenv("HOSTNAME");
            if (envHost != null && !envHost.isBlank())
                return envHost;
            return "Unknown-Device";
        }
    }

    private String readBody(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            System.out.println("HTTP request body: '" + body + "'");
            return body;
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte bytes[] = json.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendPlain(HttpExchange exchange, int status, String text) throws IOException {
        byte bytes[] = text.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "text/plain; charsets=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private boolean appendToAuthKeys(String pubkey, String comment) {
        try {
            String combined = (comment != null && !comment.isBlank()) ? pubkey + " " + comment : pubkey;
            WritePubkey.installkey(combined);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
