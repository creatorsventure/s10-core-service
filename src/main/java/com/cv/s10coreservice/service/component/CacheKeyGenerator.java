package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.constant.ApplicationConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component("cacheKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

    @Value("${spring.application.name}")
    private String appName;

    private final Sha256HashComponent hashComponent;

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder keyBuilder = new StringBuilder();

        // 1. Add Application Name
        keyBuilder.append(appName != null ? appName : "app").append(":");

        // 2. Add Class and Method Name
        keyBuilder.append(target.getClass().getSimpleName())
                .append("_")
                .append(method.getName())
                .append("_");

        // 3. Handle Parameters
        String paramString;
        if (params.length > 0) {
            paramString = Arrays.stream(params)
                    .map(param -> param != null ? param.toString() : "null")
                    .reduce((a, b) -> a + "_" + b)
                    .orElse("no-params");
        } else {
            paramString = ApplicationConstant.NOT_APPLICABLE;
        }

        // 5. Final Key
        keyBuilder.append("hash_").append(hashComponent.hash(paramString));

        return keyBuilder.toString().toLowerCase();
    }
}
