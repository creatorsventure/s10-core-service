package com.cv.s10coreservice.config.props;

import com.cv.s10coreservice.dto.KeyAliasConfigDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "security")
public class CoreSecurityProperties {
    private String keystorePath;
    private String keystorePassword;
    private KeyAliasConfigDto keystoreCurrentAlias;
    private List<KeyAliasConfigDto> keystoreOldAliases;
    private String JWTSecret;
    private Long JWTAccessTokenExpirationMins;
    private Long JWTRefreshTokenExpirationMins;
}
