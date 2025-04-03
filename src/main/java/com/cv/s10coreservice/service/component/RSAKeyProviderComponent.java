package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.config.props.CoreSecurityProperties;
import com.cv.s10coreservice.dto.KeyAliasConfigDto;
import com.cv.s10coreservice.util.StaticUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Slf4j
@Component
public class RSAKeyProviderComponent {

    private final CoreSecurityProperties properties;
    private final Map<String, KeyPair> keyVersionMap = new HashMap<>();
    private final static String KEYSTORE_INSTANCE = "PKCS12";


    @PostConstruct
    public void loadKeys() throws Exception {

        log.info("RSAKeyProviderComponent BC is registered: {}", StaticUtil.registerBouncyCastle());

        KeyStore keystore = KeyStore.getInstance(KEYSTORE_INSTANCE, BouncyCastleProvider.PROVIDER_NAME);

        log.info("Loading keystore from {}", properties.getKeystorePath());
        Resource keystoreResource = new org.springframework.core.io.DefaultResourceLoader()
                .getResource(properties.getKeystorePath());

        keystore.load(keystoreResource.getInputStream(),
                properties.getKeystorePassword().toCharArray());

        // Load current key
        log.info("Loading current keys from {}", properties.getKeystoreCurrentAlias());
        KeyAliasConfigDto current = properties.getKeystoreCurrentAlias();
        Certificate cert = keystore.getCertificate(current.getName());
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = (PrivateKey) keystore.getKey(
                current.getName(),
                current.getKeyPassword().toCharArray()
        );
        keyVersionMap.put(current.getName(), new KeyPair(publicKey, privateKey));

        // Load old aliases
        for (KeyAliasConfigDto alias : properties.getKeystoreOldAliases()) {
            log.info("Loading old keys from {}", alias);
            Certificate oldCert = keystore.getCertificate(alias.getName());
            PublicKey oldPublicKey = oldCert.getPublicKey();
            PrivateKey oldPrivateKey = (PrivateKey) keystore.getKey(
                    alias.getName(),
                    alias.getKeyPassword().toCharArray()
            );
            keyVersionMap.put(alias.getName(), new KeyPair(oldPublicKey, oldPrivateKey));
        }
    }

    public KeyPair getKeyPair(String alias) {
        return keyVersionMap.get(alias);
    }

    public String getCurrentKeyVersion() {
        return properties.getKeystoreCurrentAlias().getName();
    }
}
