package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.util.StaticUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class CommunicationSecurity {

    private static final String KEY_PAIR_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String CERTIFICATE_TYPE = "X.509";

    private static final Base64.Encoder BASE64_ENCODER = Base64.getMimeEncoder(64, new byte[]{'\n'});
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    private final SecureRandom secureRandom = new SecureRandom();
    private final CertificateFactory certificateFactory;
    private final KeyFactory rsaKeyFactory;

    public CommunicationSecurity() throws CertificateException, NoSuchAlgorithmException {
        this.certificateFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
        this.rsaKeyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM);
    }

    @PostConstruct
    public void init() {
        log.info("CommunicationSecurity BC is registered: {}", StaticUtil.registerBouncyCastle());
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        generator.initialize(2048, secureRandom);
        return generator.generateKeyPair();
    }

    public String encryptPrivateKey(PrivateKey privateKey, String password) {
        try {
            OutputEncryptor encryptor = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC)
                    .setRandom(secureRandom)
                    .setPassword(password.toCharArray())
                    .build();
            PemObject pemObject = new JcaPKCS8Generator(privateKey, encryptor).generate();
            StringWriter writer = new StringWriter();  // Moved outside
            try (PemWriter pemWriter = new PemWriter(writer)) {
                pemWriter.writeObject(pemObject);
            }
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Error encrypting private key", e);
        }
    }

    public PrivateKey loadPrivateKeyFromEncryptedPEM(String pem, String password) {
        try (PemReader reader = new PemReader(new StringReader(pem))) {
            PemObject pemObject = reader.readPemObject();
            PKCS8EncryptedPrivateKeyInfo encryptedInfo = new PKCS8EncryptedPrivateKeyInfo(pemObject.getContent());
            InputDecryptorProvider decryptor = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(password.toCharArray());
            PrivateKeyInfo keyInfo = encryptedInfo.decryptPrivateKeyInfo(decryptor);
            return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyInfo.getEncoded()));
        } catch (Exception e) {
            throw new IllegalStateException("Error loading encrypted private key", e);
        }
    }

    public X509Certificate generateCertificate(KeyPair keyPair,
                                               String commonName,
                                               String organization,
                                               String organizationalUnit,
                                               String locality,
                                               String state,
                                               String country,
                                               String email,
                                               int validityYears) {
        try {
            Date startDate = new Date();
            Date expiryDate = Date.from(ZonedDateTime.now().plusYears(validityYears).toInstant());
            BigInteger serial = new BigInteger(64, secureRandom);
            String dn = String.format(
                    "CN=%s, O=%s, OU=%s, L=%s, ST=%s, C=%s, EMAILADDRESS=%s",
                    commonName, organization, organizationalUnit, locality, state, country, email
            );
            X500Name subject = new X500Name(dn);
            X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(subject, serial, startDate, expiryDate, subject, keyPair.getPublic());
            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(keyPair.getPrivate());
            return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(builder.build(signer));
        } catch (Exception e) {
            throw new IllegalStateException("Error generating certificate", e);
        }
    }

    public String generateCertificatePem(KeyPair keyPair,
                                         String commonName,
                                         String organization,
                                         String organizationalUnit,
                                         String locality,
                                         String state,
                                         String country,
                                         String email,
                                         int validityYears) throws CertificateEncodingException {
        return toPEM(generateCertificate(keyPair,
                commonName,
                organization,
                organizationalUnit,
                locality,
                state,
                country,
                email,
                validityYears));
    }

    public X509Certificate signCSR(KeyPair issuerKeyPair, X509Certificate issuerCert, PublicKey consumerPublicKey, String consumerDn, int validityDays) {
        try {
            Date start = new Date();
            Date end = new Date(start.getTime() + (long) validityDays * 86400000);
            BigInteger serial = new BigInteger(64, secureRandom);
            X500Name issuer = new X500Name(issuerCert.getSubjectX500Principal().getName());
            X500Name subject = new X500Name(consumerDn);
            X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer, serial, start, end, subject, consumerPublicKey);
            ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(issuerKeyPair.getPrivate());
            return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(builder.build(signer));
        } catch (Exception e) {
            throw new IllegalStateException("Error signing CSR", e);
        }
    }

    public String generateCSR(KeyPair keyPair, String dn) throws Exception {
        X500Name subject = new X500Name(dn);
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());
        PKCS10CertificationRequest csr = builder.build(signer);
        return "-----BEGIN CERTIFICATE REQUEST-----\n" + BASE64_ENCODER.encodeToString(csr.getEncoded()) + "\n-----END CERTIFICATE REQUEST-----";
    }

    public PKCS10CertificationRequest parseCSR(String pem) throws IOException {
        byte[] decoded = BASE64_DECODER.decode(cleanPem(pem));
        return new PKCS10CertificationRequest(decoded);
    }

    public String getSubjectFromCSR(PKCS10CertificationRequest csr) {
        return csr.getSubject().toString();
    }

    public PublicKey extractPublicKeyFromCSR(PKCS10CertificationRequest csr) throws Exception {
        return new JcaPKCS10CertificationRequest(csr).getPublicKey();
    }

    public String toPEM(Key key) {
        return "-----BEGIN " + (key instanceof PrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----\n"
                + BASE64_ENCODER.encodeToString(key.getEncoded())
                + "\n-----END " + (key instanceof PrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----";
    }

    public String toPEM(X509Certificate cert) throws CertificateEncodingException {
        return "-----BEGIN CERTIFICATE-----\n" + BASE64_ENCODER.encodeToString(cert.getEncoded()) + "\n-----END CERTIFICATE-----";
    }

    public X509Certificate fromPEM(String pem) throws CertificateException {
        byte[] decoded = BASE64_DECODER.decode(cleanPem(pem));
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decoded));
    }

    public PrivateKey loadPrivateKeyFromPEM(String pem) {
        try {
            byte[] decoded = BASE64_DECODER.decode(cleanPem(pem));
            return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load unencrypted private key", e);
        }
    }

    public String encrypt(String data, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public String decrypt(String base64Encoded, PrivateKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(base64Encoded)));
    }

    public String sign(String data, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(key);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public boolean verify(String data, String b64EncodedSign, X509Certificate issuerCert, X509Certificate signerCert) throws Exception {
        logCertificateMetadata(signerCert);
        signerCert.checkValidity();
        signerCert.verify(issuerCert.getPublicKey());
        Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
        verifier.initVerify(signerCert.getPublicKey());
        verifier.update(data.getBytes());
        return verifier.verify(Base64.getDecoder().decode(b64EncodedSign));
    }

    public String cleanPem(String pem) {
        return pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
    }

    public void logCertificateMetadata(X509Certificate cert) {
        log.info("📄 Certificate Info:");
        log.info("   ➤ Subject : {}", cert.getSubjectX500Principal());
        log.info("   ➤ Issuer  : {}", cert.getIssuerX500Principal());
        log.info("   ➤ Valid From: {}", cert.getNotBefore());
        log.info("   ➤ Valid Until: {}", cert.getNotAfter());
        if (new Date().after(cert.getNotAfter())) {
            log.warn("⚠️ Certificate has expired.");
        }
    }
}
