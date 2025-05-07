package com.cv.s10coreservice.service.component;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class CommunicationSecurityExample {

    private static final CommunicationSecurity security;

    static {
        try {
            security = new CommunicationSecurity();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PASSWORD = "changeit"; // secure this in real apps

    public static void main(String[] args) throws Exception {
        security.init();
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("🔐 Secure Communication Simulator");

        do {
            System.out.println("\nSelect an operation:");
            System.out.println("1 - Setup Merchant (Key + CSR)");
            System.out.println("2 - Setup Bank (Root Certificate Authority + Signed Merchant Certificate)");
            System.out.println("3 - Simulate Merchant Transaction");
            System.out.println("4 - Simulate Bank Processing");
            System.out.println("0 - Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // clear newline

            switch (choice) {
                case 1 -> merchantSetup();
                case 2 -> bankSetup();
                case 3 -> simulateMerchantTransaction();
                case 4 -> simulateBankTransaction();
                case 0 -> System.out.println("Exiting...");
                default -> System.out.println("❌ Invalid choice. Try again.");
            }

        } while (choice != 0);
    }

    public static void merchantSetup() throws Exception {
        System.out.println("\n🚀 MERCHANT SETUP STARTED");
        KeyPair merchantKeyPair = security.generateKeyPair();
        String merchantDn = "CN=merchant1.com, O=Merchant Ltd, OU=Payments, L=Mumbai, ST=MH, C=IN, EMAILADDRESS=info@merchant.com";
        String merchantCsrPem = security.generateCSR(merchantKeyPair, merchantDn);

        Files.writeString(Path.of("merchant1-csr.pem"), merchantCsrPem);
        Files.writeString(Path.of("merchant1-private-not-encrypted.pem"), security.toPEM(merchantKeyPair.getPrivate()));
        Files.writeString(Path.of("merchant1-private.pem"), security.encryptPrivateKey(merchantKeyPair.getPrivate(), PASSWORD));
        System.out.println("✅ Merchant CSR and private key saved.");
    }

    public static void bankSetup() throws Exception {
        System.out.println("\n🏦 BANK SETUP STARTED");
        KeyPair bankRootKeyPair = security.generateKeyPair();
        X509Certificate bankRootCert = security.generateCertificate(
                bankRootKeyPair, "bank.com", "Bank Ltd", "CA Dept", "New York",
                "NY", "US", "root@bank.com", 10);

        Files.writeString(Path.of("bank-root-cert.pem"), security.toPEM(bankRootCert));
        Files.writeString(Path.of("bank-root-private-not-encrypted.pem"), security.toPEM(bankRootKeyPair.getPrivate()));
        Files.writeString(Path.of("bank-root-private.pem"), security.encryptPrivateKey(bankRootKeyPair.getPrivate(), PASSWORD));

        String merchantCsrPem = Files.readString(Path.of("merchant1-csr.pem"));
        PKCS10CertificationRequest merchantCsr = security.parseCSR(merchantCsrPem);
        String merchantSubjectDn = security.getSubjectFromCSR(merchantCsr);
        PublicKey merchantPublicKey = security.extractPublicKeyFromCSR(merchantCsr);

        X509Certificate signedMerchantCert = security.signCSR(
                bankRootKeyPair, bankRootCert, merchantPublicKey, merchantSubjectDn, 1);
        Files.writeString(Path.of("bank-signed-merchant1-cert.pem"), security.toPEM(signedMerchantCert));
        System.out.println("✅ Signed merchant certificate generated and stored.");
    }

    public static void simulateMerchantTransaction() throws Exception {
        System.out.println("\n📤 MERCHANT TRANSACTION INITIATED");
        String message = "{ orderId :1234, amount :100.0}";

        String encryptedPrivateKeyPEM = Files.readString(Path.of("merchant1-private.pem"));
        PrivateKey merchantPrivateKey = security.loadPrivateKeyFromEncryptedPEM(encryptedPrivateKeyPEM, PASSWORD);
        X509Certificate bankCert = security.parseCertificateFromPEM(Files.readString(Path.of("bank-root-cert.pem")));

        String signature = security.sign(message, merchantPrivateKey);
        String encrypted = security.encrypt(message, bankCert.getPublicKey());

        Files.write(Path.of("message.encrypted"), encrypted.getBytes());
        Files.write(Path.of("message.signature"), signature.getBytes());

        System.out.println("✅ Message encrypted and signed by merchant.");
    }

    public static void simulateBankTransaction() throws Exception {
        System.out.println("\n📥 BANK PROCESSING MESSAGE");

        String encrypted = Files.readString(Path.of("message.encrypted"));
        String signature = Files.readString(Path.of("message.signature"));

        String encryptedPrivateKeyPEM = Files.readString(Path.of("bank-root-private.pem"));
        PrivateKey bankPrivateKey = security.loadPrivateKeyFromEncryptedPEM(encryptedPrivateKeyPEM, PASSWORD);
        X509Certificate bankCert = security.parseCertificateFromPEM(Files.readString(Path.of("bank-root-cert.pem")));
        X509Certificate merchantCert = security.parseCertificateFromPEM(Files.readString(Path.of("bank-signed-merchant1-cert.pem")));

        String decrypted = security.decrypt(encrypted, bankPrivateKey);
        boolean verified = security.verify(decrypted, signature, bankCert, merchantCert);

        System.out.println("🧾 Message: " + decrypted);
        System.out.println("✅ Signature verified: " + verified);
    }
}
