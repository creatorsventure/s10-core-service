package com.cv.s10coreservice.service.component;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class Sha256HashComponent {

    private final ThreadLocal<MessageDigest> threadLocalDigest = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    });

    public String hash(String input) {
        byte[] hashBytes = threadLocalDigest.get().digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes); // Requires Java 17+
    }

    public String hashWithSalt(String input, String salt) {
        return hash(salt + input);
    }
}
