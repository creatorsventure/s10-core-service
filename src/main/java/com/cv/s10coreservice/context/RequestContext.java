package com.cv.s10coreservice.context;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, Object> getAll() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
