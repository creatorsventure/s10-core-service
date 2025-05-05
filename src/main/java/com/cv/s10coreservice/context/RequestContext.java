package com.cv.s10coreservice.context;

import com.cv.s10coreservice.constant.ApplicationConstant;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, String value) {
        CONTEXT.get().put(key, value);
    }

    public static String get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, String> getAll() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static String getUnitId() {
        return CONTEXT.get().get(ApplicationConstant.HIBERNATE_PARAM_UNIT_ID);
    }

    public static String getMerchantId() {
        return CONTEXT.get().get(ApplicationConstant.HIBERNATE_PARAM_MERCHANT_ID);
    }
}
