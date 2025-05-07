package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.util.StaticUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class CommunicationSecurity {

    @PostConstruct
    public void init() {
        log.info("CommunicationSecurity BC is registered: {}", StaticUtil.registerBouncyCastle());
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public X509Certificate generateCertificate(
            KeyPair keyPair,
            String commonName,
            String organization,
            String organizationalUnit,
            String locality,
            String state,
            String country,
            String email,
            int validityDays) {

        try {
            long now = System.currentTimeMillis();
            Date startDate = new Date(now);
            Date expiryDate = new Date(now + (long) validityDays * 24 * 60 * 60 * 1000L);

            BigInteger serialNumber = new BigInteger(64, new SecureRandom());

            String dn = String.format(
                    "CN=%s, O=%s, OU=%s, L=%s, ST=%s, C=%s, EMAILADDRESS=%s",
                    commonName, organization, organizationalUnit, locality, state, country, email
            );
            X500Name subject = new X500Name(dn);

            X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    subject,
                    serialNumber,
                    startDate,
                    expiryDate,
                    subject,
                    keyPair.getPublic()
            );

            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC")
                    .build(keyPair.getPrivate());

            return new JcaX509CertificateConverter()
                    .setProvider("BC")
                    .getCertificate(certBuilder.build(signer));

        } catch (Exception ex) {
            throw new RuntimeException("❌ Failed to generate X.509 certificate", ex);
        }
    }

    public X509Certificate signCSR(KeyPair issuerKeyPair,
                                   X509Certificate issuerCertificate,
                                   PublicKey consumerPublicKey,
                                   String consumerDn,
                                   int validityDays) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Date expiryDate = new Date(now + (long) validityDays * 24 * 60 * 60 * 1000L);

        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        X500Name issuer = new X500Name(issuerCertificate.getSubjectX500Principal().getName());
        X500Name subject = new X500Name(consumerDn);

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serialNumber,
                startDate,
                expiryDate,
                subject,
                consumerPublicKey
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(issuerKeyPair.getPrivate());

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
    }

    public String generateCSR(KeyPair keyPair, String subjectDN) throws Exception {
        X500Name subject = new X500Name(subjectDN);
        ContentSigner signGen = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
        PKCS10CertificationRequest csr = builder.build(signGen);
        return "-----BEGIN CERTIFICATE REQUEST-----\n" +
                Base64.getEncoder().encodeToString(csr.getEncoded()) +
                "\n-----END CERTIFICATE REQUEST-----";
    }

    public PKCS10CertificationRequest parseCSR(String pem) throws Exception {
        String cleanPem = pem.replace("-----BEGIN CERTIFICATE REQUEST-----", "")
                .replace("-----END CERTIFICATE REQUEST-----", "")
                .replaceAll("\n", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
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
                + Base64.getEncoder().encodeToString(key.getEncoded()) + "\n-----END " + (key instanceof PrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----";
    }

    public String toPEM(X509Certificate certificate) throws CertificateEncodingException {
        return "-----BEGIN CERTIFICATE-----\n"
                + Base64.getEncoder().encodeToString(certificate.getEncoded())
                + "\n-----END CERTIFICATE-----";
    }

    public X509Certificate parseCertificateFromPEM(String pem) throws CertificateException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        String cleanPem = pem.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\n", "");
        byte[] decoded = Base64.getDecoder().decode(cleanPem);
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decoded));
    }

    public PrivateKey loadPrivateKeyFromPEM(String pem) {
        try {
            String cleanPem = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load private key from PEM", e);
        }
    }

    public byte[] encrypt(byte[] plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(ciphertext);
    }

    public byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public boolean verify(byte[] data,
                          byte[] sig,
                          X509Certificate issuerCertificate,
                          X509Certificate consumerCertificate) throws Exception {
        logCertificateMetadata(consumerCertificate);
        consumerCertificate.checkValidity();
        consumerCertificate.verify(issuerCertificate.getPublicKey());
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(consumerCertificate.getPublicKey());
        signature.update(data);
        return signature.verify(sig);
    }

    public void logCertificateMetadata(X509Certificate cert) {
        log.info("📄 Certificate Info:");
        log.info("   ➤ Subject : {}", cert.getSubjectX500Principal());
        log.info("   ➤ Issuer  : {}", cert.getIssuerX500Principal());
        log.info("   ➤ Valid From: {}", cert.getNotBefore());
        log.info("   ➤ Valid Until: {}", cert.getNotAfter());

        Date now = new Date();
        if (now.after(cert.getNotAfter())) {
            log.info("⚠️ Certificate has expired.");
        }
    }

}
