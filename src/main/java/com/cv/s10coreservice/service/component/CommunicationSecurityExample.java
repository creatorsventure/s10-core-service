package com.cv.s10coreservice.service.component;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class CommunicationSecurityExample {

    private static final CommunicationSecurity security = new CommunicationSecurity();

    public static void main(String[] args) throws Exception {
        // Step 0: Register BouncyCastle provider
        security.init();

        simulateMerchant();
        simulateBank();
        simulateMerchantTransaction();
        simulateBankTransaction();
    }

    public static void simulateMerchant() throws Exception {
        System.out.println("\n🚀 MERCHANT SETUP STARTED");
        KeyPair merchantKeyPair = security.generateKeyPair();
        String merchantDn = "CN=merchant1.com, O=Merchant Ltd, OU=Payments, L=Mumbai, ST=MH, C=IN, EMAILADDRESS=info@merchant.com";
        String merchantCsrPem = security.generateCSR(merchantKeyPair, merchantDn);
        Files.writeString(Path.of("merchant1-csr.pem"), merchantCsrPem);
        Files.writeString(Path.of("merchant1-private.pem"), security.toPEM(merchantKeyPair.getPrivate()));
    }

    public static void simulateBank() throws Exception {
        System.out.println("\n🏦 BANK SETUP STARTED");
        KeyPair bankRootKeyPair = security.generateKeyPair();
        X509Certificate bankRootCert = security.generateCertificate(
                bankRootKeyPair, "bank.com", "Bank Ltd", "CA Dept", "New York",
                "NY", "US", "root@bank.com", 3650);

        Files.writeString(Path.of("bank-root-cert.pem"), security.toPEM(bankRootCert));
        Files.writeString(Path.of("bank-root-private.pem"), security.toPEM(bankRootKeyPair.getPrivate()));

        String merchantCsrPem = Files.readString(Path.of("merchant1-csr.pem"));
        PKCS10CertificationRequest merchantCsr = security.parseCSR(merchantCsrPem);
        String merchantSubjectDn = security.getSubjectFromCSR(merchantCsr);
        PublicKey merchantPublicKey = security.extractPublicKeyFromCSR(merchantCsr);

        X509Certificate signedMerchantCert = security.signCSR(
                bankRootKeyPair, bankRootCert, merchantPublicKey, merchantSubjectDn, 365);

        Files.writeString(Path.of("signed-merchant1-cert.pem"), security.toPEM(signedMerchantCert));
    }

    public static void simulateMerchantTransaction() throws Exception {
        System.out.println("\n📤 MERCHANT TRANSACTION INITIATED");
        String message = "{ orderId :1234, amount :100.0}";
        byte[] plaintext = message.getBytes();

        PrivateKey merchantPrivateKey = security.loadPrivateKeyFromPEM("merchant1-private.pem");
        X509Certificate bankCert = security.parseCertificateFromPEM(Files.readString(Path.of("bank-root-cert.pem")));

        byte[] signature = security.sign(plaintext, merchantPrivateKey);
        byte[] encrypted = security.encrypt(plaintext, bankCert.getPublicKey());

        Files.write(Path.of("message.encrypted"), encrypted);
        Files.write(Path.of("message.signature"), signature);

        System.out.println("✅ Message encrypted and signed by merchant.");
    }

    public static void simulateBankTransaction() throws Exception {
        System.out.println("\n📥 BANK PROCESSING MESSAGE");

        byte[] encrypted = Files.readAllBytes(Path.of("message.encrypted"));
        byte[] signature = Files.readAllBytes(Path.of("message.signature"));

        PrivateKey bankPrivateKey = security.loadPrivateKeyFromPEM("bank-root-private.pem");
        X509Certificate bankCert = security.parseCertificateFromPEM(Files.readString(Path.of("bank-root-cert.pem")));
        X509Certificate merchantCert = security.parseCertificateFromPEM(Files.readString(Path.of("signed-merchant1-cert.pem")));

        byte[] decrypted = security.decrypt(encrypted, bankPrivateKey);
        boolean verified = security.verify(decrypted, signature, bankCert, merchantCert);

        String message = new String(decrypted);
        System.out.println("🧾 Message: " + message);
        System.out.println("✅ Signature verified: " + verified);
    }
}
