package com.cv.s10coreservice.util;

import com.cv.s10coreservice.constant.ApplicationConstant;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Security;

public class StaticUtil {

    public static boolean isSearchRequest(String searchField, String searchValue) {
        return StringUtils.hasText(searchField) && StringUtils.hasText(searchValue);
    }

    private static boolean initialized = false;
    public static synchronized boolean registerBouncyCastle() {
        if (!initialized && Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            initialized = true;
        }
        return initialized;
    }

    public static String extractHeader(String header) throws Exception {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(header)
                : ApplicationConstant.NOT_APPLICABLE;
    }
}
