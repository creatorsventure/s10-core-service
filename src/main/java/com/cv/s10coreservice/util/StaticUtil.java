package com.cv.s10coreservice.util;

import com.cv.s10coreservice.constant.ApplicationConstant;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class StaticUtil {

    public static boolean isSearchRequest(String searchField, String searchValue) {
        return StringUtils.hasText(searchField) && StringUtils.hasText(searchValue);
    }

    public static String extractHeader(String header) throws Exception {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(header)
                : ApplicationConstant.NOT_APPLICABLE;
    }
}
