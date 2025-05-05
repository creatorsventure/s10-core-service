package com.cv.s10coreservice.service.component;

import com.cv.s10coreservice.dto.APIResponseDto;
import com.cv.s10coreservice.exception.ExceptionComponent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class APIServiceCaller {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final ExceptionComponent exceptionComponent;

    // ✅ Main entry point to dynamically call any client and parse response (non-generic)
    public <C, R> R call(Class<C> clientClass,
                         Function<C, ResponseEntity<String>> clientCall,
                         Class<R> responseType) {
        C client = applicationContext.getBean(clientClass);
        ResponseEntity<String> response = clientCall.apply(client);
        return parse(response, responseType);
    }

    // ✅ Dynamically call and parse generic types
    public <C, R> R call(Class<C> clientClass,
                         Function<C, ResponseEntity<String>> clientCall,
                         TypeReference<R> responseTypeRef) {
        C client = applicationContext.getBean(clientClass);
        ResponseEntity<String> response = clientCall.apply(client);
        return parse(response, responseTypeRef);
    }

    // ✅ Parse from ResponseEntity (non-generic)
    public <T> T parse(ResponseEntity<String> response, Class<T> clazz) {
        return parse(response.getBody(), clazz);
    }

    // ✅ Parse from ResponseEntity (generic)
    public <T> T parse(ResponseEntity<String> response, TypeReference<T> typeRef) {
        return parse(response::getBody, typeRef);
    }

    // ✅ Parse from Supplier<String> (non-generic)
    public <T> T parse(Supplier<String> supplier, Class<T> clazz) {
        return parse(supplier.get(), clazz);
    }

    // ✅ Parse from Supplier<String> (generic)
    public <T> T parse(Supplier<String> supplier, TypeReference<T> typeRef) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(typeRef.getType());
        return parse(supplier.get(), javaType);
    }

    // ✅ Parse non-generic response from raw string
    public <T> T parse(String response, Class<T> responseType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response DTO (Class): {}", apiResponseDto);
            if (apiResponseDto.isStatus()) {
                JsonNode objectNode = objectMapper.valueToTree(apiResponseDto.getObject());
                return objectMapper.treeToValue(objectNode, responseType);
            } else {
                throw exceptionComponent.expose("app.message.internal.api.failure", true);
            }
        } catch (Exception ex) {
            log.error("❌ Failed to parse API response (Class): {}", ExceptionUtils.getStackTrace(ex));
            throw exceptionComponent.expose("app.message.internal.api.failure", true);
        }
    }

    // ✅ Parse generic response from raw string
    public <T> T parse(String response, JavaType responseType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response DTO (JavaType): {}", apiResponseDto);
            if (apiResponseDto.isStatus()) {
                return objectMapper.convertValue(apiResponseDto.getObject(), responseType);
            } else {
                throw exceptionComponent.expose("app.message.internal.api.failure", true);
            }
        } catch (Exception ex) {
            log.error("❌ Failed to parse API response (JavaType): {}", ExceptionUtils.getStackTrace(ex));
            throw exceptionComponent.expose("app.message.internal.api.failure", true);
        }
    }
}
