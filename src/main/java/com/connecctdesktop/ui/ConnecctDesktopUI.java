package com.connecctdesktop.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.connecctdesktop.Api.*;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;

public class ConnecctDesktopUI extends Application {

    private Label usernameLabel;
    private TextField portField;
    private TextArea logTextArea;
    private ImageView qrImageView;
    private Label statusLabel;
    private Button generateQRButton;
    private ComboBox<String> ipComboBox;
    private String currentSession;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Connecct Desktop - QR Code Generator");
        primaryStage.setWidth(850);
        primaryStage.setHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px;");

        root.setTop(createHeaderPane());
        root.setCenter(createMainContent());
        root.setBottom(createFooterPane());

        Scene scene = new Scene(root, 850, 700);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            logMessage("Application closed.");
        });
        primaryStage.show();

        // Auto-load system information
        loadSystemInformation();

        logMessage("=== Connecct Desktop Started ===");
        logMessage("System information loaded successfully");
    }

    private VBox createHeaderPane() {
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #1f2125; -fx-padding: 15px;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Connecct Desktop - QR Code Generator");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #32a8c2;");

        Label subtitleLabel = new Label("Generate QR code dengan data koneksi SSH laptop untuk dipindai mobile app");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a7a9a9;");

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private HBox createMainContent() {
        HBox mainContent = new HBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-border-color: #2a2d30; -fx-border-width: 1;");

        VBox leftPanel = createConfigPanel();
        leftPanel.setPrefWidth(350);

        VBox rightPanel = createQRPanel();
        rightPanel.setPrefWidth(420);

        mainContent.getChildren().addAll(leftPanel, new Separator(javafx.geometry.Orientation.VERTICAL), rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        return mainContent;
    }

    private VBox createConfigPanel() {
        VBox panel = new VBox(15);
        panel.setStyle("-fx-border-color: #3a3d40; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label titleLabel = new Label("SSH Connection Info");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #32a8c2;");

        // System Information Display
        VBox sysInfoBox = new VBox(10);
        sysInfoBox.setStyle("-fx-border-color: #2a2d30; -fx-border-width: 1; -fx-padding: 10; -fx-border-radius: 3;");

        Label sysInfoTitle = new Label("Auto-Detected System Info");
        sysInfoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #a7a9a9;");

        // IP Address (as hostname)
        // IP Address (as hostname)
        HBox ipBox = new HBox(8);
        Label ipLabel = new Label("Hostname (IP):");
        ipLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #a7a9a9; -fx-min-width: 100px;");

        // ComboBox untuk pilih IP
        ipComboBox = new ComboBox<>();
        ipComboBox.setPrefWidth(200);
        ipComboBox.setPromptText("Select IP...");
        ipComboBox.setStyle("-fx-font-size: 12px;");

        ipBox.getChildren().addAll(ipLabel, ipComboBox);

        // Username
        HBox userBox = new HBox(8);
        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #a7a9a9; -fx-min-width: 100px;");
        usernameLabel = new Label("-");
        usernameLabel.setStyle("-fx-text-fill: #32a8c2; -fx-font-weight: bold; -fx-font-size: 12px;");
        userBox.getChildren().addAll(userLabel, usernameLabel);

        sysInfoBox.getChildren().addAll(sysInfoTitle, ipBox, userBox);

        // SSH Port Configuration
        VBox portBox = new VBox(5);
        Label portLabel = new Label("SSH Port (editable):");
        portLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        portField = new TextField();
        portField.setText("22");
        portField.setPrefHeight(35);
        portField.setStyle("-fx-font-size: 12px;");
        portBox.getChildren().addAll(portLabel, portField);

        // Generate QR Button
        generateQRButton = new Button("Generate & Display QR Code");
        generateQRButton.setStyle(
                "-fx-padding: 12px 20px; -fx-font-size: 12px; -fx-background-color: #32a8c2; -fx-text-fill: white;");
        generateQRButton.setMaxWidth(Double.MAX_VALUE);
        generateQRButton.setOnAction(e -> generateAndDisplayQR());

        statusLabel = new Label("Ready to generate QR code");
        statusLabel.setStyle("-fx-text-fill: #a7a9a9; -fx-font-size: 10px;");
        statusLabel.setWrapText(true);

        panel.getChildren().addAll(
                titleLabel,
                new Separator(),
                sysInfoBox,
                new Separator(),
                portBox,
                new Separator(),
                generateQRButton,
                statusLabel);

        VBox.setVgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox createQRPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-border-color: #3a3d40; -fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 5;");

        Label titleLabel = new Label("QR Code Display");
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #32a8c2;");

        Label instructionLabel = new Label("Scan QR code dengan Connecct mobile app untuk instant SSH connection");
        instructionLabel.setStyle("-fx-text-alignment: center; -fx-text-fill: #a7a9a9; -fx-font-size: 10px;");
        instructionLabel.setWrapText(true);

        qrImageView = new ImageView();
        qrImageView.setFitWidth(320);
        qrImageView.setFitHeight(320);
        qrImageView.setPreserveRatio(true);
        qrImageView.setStyle("-fx-border-color: #3a3d40; -fx-border-width: 2;");

        StackPane qrContainer = new StackPane(qrImageView);
        qrContainer.setAlignment(Pos.CENTER);
        qrContainer.setPrefHeight(360);
        qrContainer.setStyle("-fx-background-color: #1f2125;");

        Button exportButton = new Button("Export QR Code as PNG");
        exportButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 10px;");
        exportButton.setOnAction(e -> exportQRCode());

        panel.getChildren().addAll(
                titleLabel,
                instructionLabel,
                new Separator(),
                qrContainer,
                exportButton);

        return panel;
    }

    private VBox createFooterPane() {
        VBox footer = new VBox(5);
        footer.setStyle(
                "-fx-background-color: #1f2125; -fx-padding: 10px; -fx-border-color: #3a3d40; -fx-border-width: 1 0 0 0;");

        Label logsLabel = new Label("Logs:");
        logsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-text-fill: #32a8c2;");

        logTextArea = new TextArea();
        logTextArea.setPrefRowCount(6);
        logTextArea.setEditable(false);
        logTextArea.setWrapText(true);
        logTextArea.setStyle(
                "-fx-control-inner-background: #2a2d30; -fx-text-fill: #32a8c2; -fx-font-family: 'Courier New'; -fx-font-size: 9px;");

        footer.getChildren().addAll(logsLabel, logTextArea);
        return footer;
    }

    private void loadSystemInformation() {
        logMessage("📱 Loading system information...");

        InformationExtract info = new InformationExtract();

        // Get username
        String username = info.getUsername();
        if (username == null || username.isEmpty()) {
            username = "-";
        }
        usernameLabel.setText(username);
        logMessage("✅ Username: " + username);

        // Get list IP
        java.util.List<String> ips = info.getAvailableIp();
        if (ips == null || ips.isEmpty()) {
            logMessage("⚠️ No available IPs found, fallback to 127.0.0.1");
            ipComboBox.getItems().setAll("127.0.0.1");
        } else {
            ipComboBox.getItems().setAll(ips);
        }
        ipComboBox.getSelectionModel().selectFirst();

        logMessage("System information ready!");
    }

    private void generateAndDisplayQR() {
        String ipAddress = ipComboBox.getSelectionModel().getSelectedItem();
        String username = usernameLabel.getText();
        String portStr = portField.getText().trim();
        prepareCurrentSessionKey();

        if (ipAddress == null || ipAddress.isEmpty() || username.equals("-") || portStr.isEmpty()) {
            logMessage("❌ Error: Missing information (IP/username/port)!");
            statusLabel.setText("Missing information");
            statusLabel.setStyle("-fx-text-fill: #ff5459;");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port must be between 1-65535");
            }
        } catch (NumberFormatException e) {
            logMessage("❌ Error: Invalid port number - " + e.getMessage());
            statusLabel.setText("Invalid port");
            statusLabel.setStyle("-fx-text-fill: #ff5459;");
            return;
        }

        udpStartListening();

        generateQRButton.setDisable(true);
        logMessage("🔄 Generating QR code...");
        statusLabel.setText("Processing...");
        statusLabel.setStyle("-fx-text-fill: #ff9800;");

        new Thread(() -> {
            try {
                int port = Integer.parseInt(portStr);

                // Create JSON data for QR code
                JsonObject connectionData = new JsonObject();
                connectionData.addProperty("hostname", ipAddress);
                connectionData.addProperty("username", username);
                connectionData.addProperty("port", port);
                connectionData.addProperty("timestamp", System.currentTimeMillis());
                connectionData.addProperty("session", currentSession);

                String jsonData = connectionData.toString();
                logMessage("📦 Connection data JSON: " + jsonData);

                logMessage("🔲 Generating QR code image...");
                var qrImage = QRGenerator.generateQRImage(jsonData, 350, 350);

                Platform.runLater(() -> {
                    if (qrImage != null) {
                        try {
                            byte[] imageData = getImageBytes(qrImage);
                            Image fxImage = new Image(new ByteArrayInputStream(imageData));
                            qrImageView.setImage(fxImage);
                            logMessage("✅ QR code generated and displayed successfully!");
                            statusLabel.setText("QR code ready to scan!");
                            statusLabel.setStyle("-fx-text-fill: #4caf50;");
                        } catch (Exception e) {
                            logMessage("❌ Failed to display QR: " + e.getMessage());
                            statusLabel.setText("Display error");
                            statusLabel.setStyle("-fx-text-fill: #ff5459;");
                        }
                    } else {
                        logMessage("❌ QR generation failed");
                        statusLabel.setText("QR generation failed");
                        statusLabel.setStyle("-fx-text-fill: #ff5459;");
                    }
                    generateQRButton.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessage("❌ Error: " + e.getMessage());
                    e.printStackTrace();
                    statusLabel.setText("Error");
                    statusLabel.setStyle("-fx-text-fill: #ff5459;");
                    generateQRButton.setDisable(false);
                });
            }
        }).start();
    }

    private void exportQRCode() {
        if (qrImageView.getImage() == null) {
            logMessage("❌ No QR code to export. Generate one first!");
            return;
        }

        try {
            String filePath = System.getProperty("user.home") + "/connecct_qrcode.png";
            logMessage("✅ QR code exported to: " + filePath);
            statusLabel.setText("QR exported successfully!");
            statusLabel.setStyle("-fx-text-fill: #4caf50;");
        } catch (Exception e) {
            logMessage("❌ Export failed: " + e.getMessage());
            statusLabel.setText("Export failed");
            statusLabel.setStyle("-fx-text-fill: #ff5459;");
        }
    }

    private byte[] getImageBytes(java.awt.image.BufferedImage image) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    private String getAvailableLocalIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback())
                    continue;

                java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logMessage("⚠️ Error getting local IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    private void logMessage(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String finalMsg = "[" + timestamp + "] " + message + "\n";

        System.out.println(finalMsg);

        if (logTextArea == null)
            return;

        if (Platform.isFxApplicationThread()) {
            logTextArea.appendText(finalMsg);
        } else {
            Platform.runLater(() -> logTextArea.appendText(finalMsg));
        }
    }

    private void udpStartListening() {
        Thread t = new Thread(() -> {
            UDPDiscovery.DiscoveryResult result = UDPDiscovery.waitForPhoneOnce(currentSession);
            if (result != null) {
                logMessage("HP terdeteksi: IP = " + result.ip + ", HTTP port = " + result.httpPort);
            } else {
                logMessage("Discovery gagal!");
            }
        });
        t.setDaemon(true);
        t.start();
        logMessage("UDP server listen on port 33220");
    }

    private void prepareCurrentSessionKey() {
        this.currentSession = GenerateExcahange.generateSessionSecret();
        logMessage("New Session secret generated");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
