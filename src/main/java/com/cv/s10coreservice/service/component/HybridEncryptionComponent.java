package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.util.StaticUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

@AllArgsConstructor
@Slf4j
@Component
public class HybridEncryptionComponent {

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // 96-bit IV
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final String DELIMITER = ":::";
    private static final String KEY_GENERATION_ALGORITHM = "AES";
    private static final String DATA_ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private final RSAKeyProviderComponent keyProvider;

    @PostConstruct
    public void init() {
        log.info("HybridEncryptionComponent BC is registered: {}", StaticUtil.registerBouncyCastle());
    }

    public String encrypt(String plainText) throws Exception {

        // Step 1: Generate AES key
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_GENERATION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        keyGen.init(AES_KEY_SIZE);
        SecretKey aesKey = keyGen.generateKey();

        // Step 2: Generate IV
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = SecureRandom.getInstanceStrong();
        random.nextBytes(iv);

        // Step 3: Encrypt data with AES-GCM
        Cipher aesCipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);
        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes());

        // Step 4: Encrypt AES key with the current RSA public key
        String currentAlias = keyProvider.getCurrentKeyVersion();
        PublicKey publicKey = keyProvider.getKeyPair(currentAlias).getPublic();

        Cipher rsaCipher = Cipher.getInstance(KEY_ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        // Step 5: Combine all parts with the key alias and perform an outer Base64 encoding
        String combined = currentAlias + DELIMITER
                + Base64.getEncoder().encodeToString(encryptedData) + DELIMITER
                + Base64.getEncoder().encodeToString(encryptedAesKey) + DELIMITER
                + Base64.getEncoder().encodeToString(iv);

        return Base64.getEncoder().encodeToString(combined.getBytes());
    }

    public String decrypt(String encodedPayload) throws Exception {
        // Step 1: Base64-decode the outer wrapper
        String decoded = new String(Base64.getDecoder().decode(encodedPayload));
        String[] parts = decoded.split(DELIMITER);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid payload format");
        }

        // Step 2: Extract the key alias and other parts from the payload
        String keyAlias = parts[0];
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
        byte[] encryptedAesKey = Base64.getDecoder().decode(parts[2]);
        byte[] iv = Base64.getDecoder().decode(parts[3]);

        // Step 3: Use the alias to look up the correct RSA private key for decryption
        PrivateKey privateKey = keyProvider.getKeyPair(keyAlias).getPrivate();

        // Step 4: Decrypt the AES key using the RSA private key
        Cipher rsaCipher = Cipher.getInstance(KEY_ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, KEY_GENERATION_ALGORITHM);

        // Step 5: Decrypt the data using AES-GCM
        Cipher aesCipher = Cipher.getInstance(DATA_ENCRYPTION_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
        byte[] plainText = aesCipher.doFinal(encryptedData);

        return new String(plainText);
    }
}
