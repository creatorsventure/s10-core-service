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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class APIServiceCaller {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final ExceptionComponent exceptionComponent;

    // Main dynamic call method (strict)
    public <C, R> R call(Class<C> clientClass,
                         Function<C, ResponseEntity<String>> clientCall,
                         Class<R> responseType) {
        C client = applicationContext.getBean(clientClass);
        ResponseEntity<String> response = clientCall.apply(client);
        return parse(response, responseType);
    }

    public <C, R> R call(Class<C> clientClass,
                         Function<C, ResponseEntity<String>> clientCall,
                         TypeReference<R> responseTypeRef) {
        C client = applicationContext.getBean(clientClass);
        ResponseEntity<String> response = clientCall.apply(client);
        return parse(response, responseTypeRef);
    }

    // Optional-call wrapper for non-generic types
    public <C, R> Optional<R> callOptional(Class<C> clientClass,
                                           Function<C, ResponseEntity<String>> clientCall,
                                           Class<R> responseType) {
        try {
            C client = applicationContext.getBean(clientClass);
            ResponseEntity<String> response = clientCall.apply(client);
            return parseOptional(response, responseType);
        } catch (Exception ex) {
            log.warn("🔸 callOptional non-generic types failed: {}", ExceptionUtils.getStackTrace(ex));
            return Optional.empty();
        }
    }

    // Optional-call wrapper for generic types
    public <C, R> Optional<R> callOptional(Class<C> clientClass,
                                           Function<C, ResponseEntity<String>> clientCall,
                                           TypeReference<R> responseTypeRef) {
        try {
            C client = applicationContext.getBean(clientClass);
            ResponseEntity<String> response = clientCall.apply(client);
            return parseOptional(response, responseTypeRef);
        } catch (Exception ex) {
            log.warn("🔸 callOptional generic types failed: {}", ExceptionUtils.getStackTrace(ex));
            return Optional.empty();
        }
    }

    // ---- Parse section (strict) ----

    public <T> T parse(ResponseEntity<String> response, Class<T> clazz) {
        return parse(response.getBody(), clazz);
    }

    public <T> T parse(ResponseEntity<String> response, TypeReference<T> typeRef) {
        return parse(response::getBody, typeRef);
    }

    public <T> T parse(Supplier<String> supplier, Class<T> clazz) {
        return parse(supplier.get(), clazz);
    }

    public <T> T parse(Supplier<String> supplier, TypeReference<T> typeRef) {
        JavaType javaType = objectMapper.getTypeFactory().constructType(typeRef.getType());
        return parse(supplier.get(), javaType);
    }

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

    // ---- Optional parse section ----

    public <T> Optional<T> parseOptional(ResponseEntity<String> response, Class<T> clazz) {
        return parseOptional(response.getBody(), clazz);
    }

    public <T> Optional<T> parseOptional(ResponseEntity<String> response, TypeReference<T> typeRef) {
        return parseOptional(response::getBody, typeRef);
    }

    public <T> Optional<T> parseOptional(Supplier<String> supplier, TypeReference<T> typeRef) {
        return parseOptional(supplier.get(), typeRef);
    }

    public <T> Optional<T> parseOptional(String response, Class<T> responseType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response DTO (Optional/Class): {}", apiResponseDto);
            if (apiResponseDto.isStatus() && apiResponseDto.getObject() != null) {
                JsonNode objectNode = objectMapper.valueToTree(apiResponseDto.getObject());
                T result = objectMapper.treeToValue(objectNode, responseType);
                return Optional.ofNullable(result);
            }
        } catch (Exception ex) {
            log.warn("🔸 Failed to parse optional response (Class): {}", ExceptionUtils.getStackTrace(ex));
        }
        return Optional.empty();
    }

    public <T> Optional<T> parseOptional(String response, TypeReference<T> typeRef) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(typeRef.getType());
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response DTO (Optional/Generic): {}", apiResponseDto);
            if (apiResponseDto.isStatus() && apiResponseDto.getObject() != null) {
                T result = objectMapper.convertValue(apiResponseDto.getObject(), javaType);
                return Optional.ofNullable(result);
            }
        } catch (Exception ex) {
            log.warn("🔸 Failed to parse optional response (Generic): {}", ExceptionUtils.getStackTrace(ex));
        }
        return Optional.empty();
    }
}
