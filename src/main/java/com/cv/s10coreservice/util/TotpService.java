package com.cv.s10coreservice.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public final class TotpService {

    private static final GoogleAuthenticator googleAuthenticator;

    static {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setCodeDigits(6)              // 6 digits OTP
                .setTimeStepSizeInMillis(30_000) // OTP valid for 30 seconds
                .setWindowSize(1)               // Allow clock drift (one window forward/backward)
                .build();
        googleAuthenticator = new GoogleAuthenticator(config);
    }

    private TotpService() {
        // Prevent instantiation
    }

    /**
     * Generate a new secret key for a user.
     */
    public static String generateSecretKey() {
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    /**
     * Verify the provided OTP against the user's secret key.
     */
    public static boolean verifyOtp(String secretKey, int otp) {
        return googleAuthenticator.authorize(secretKey, otp);
    }

    /**
     * Generate the current OTP based on the user's secret key.
     */
    public static int generateCurrentOtp(String secretKey) {
        return googleAuthenticator.getTotpPassword(secretKey);
    }

    /**
     * Generate the current OTP as a String with leading zeros preserved.
     */
    public static String generateCurrentOtpAsString(String secretKey) {
        int otp = googleAuthenticator.getTotpPassword(secretKey);
        return String.format("%06d", otp);
    }
}
