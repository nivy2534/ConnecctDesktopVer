package com.connecctdesktop.Api;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WritePubkey {
    public WritePubkey() {

    }

    public static void installkey(String pubkey) throws IOException {
        Path home = Path.of(System.getProperty("user.home"));
        Path sshDir = home.resolve(".ssh");
        Path authKeys = sshDir.resolve("authorized_keys");

        if (!Files.exists(sshDir)) {
            Files.createDirectories(sshDir);
        }

        String line = pubkey.endsWith("\n") ? pubkey : pubkey + "\n";
        Files.write(authKeys, line.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
}
