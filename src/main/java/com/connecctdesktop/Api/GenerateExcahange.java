package com.connecctdesktop.Api;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class GenerateExcahange {
    private static final Random RNG = new SecureRandom();

    public static String generateSessionSecret() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getEncoder().withoutPadding().encodeToString(buf);
    }
}
