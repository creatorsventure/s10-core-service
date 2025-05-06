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

    // ------------------ MAIN CALLERS ------------------

    /**
     * Calls a client function that returns a String (raw response body)
     * and deserializes it to a non-generic type.
     */
    public <C, R> R call(Class<C> clientClass,
                         Function<C, String> clientCall,
                         Class<R> responseType) {
        C client = applicationContext.getBean(clientClass);
        String raw = clientCall.apply(client);
        return parse(raw, responseType);
    }

    /**
     * Calls a client function that returns a String (raw response body)
     * and deserializes it to a generic type (e.g. List<MyDto>).
     */
    public <C, R> R call(Class<C> clientClass,
                         Function<C, String> clientCall,
                         TypeReference<R> responseTypeRef) {
        C client = applicationContext.getBean(clientClass);
        String raw = clientCall.apply(client);
        return parse(raw, responseTypeRef);
    }

    /**
     * Optional version of the above (non-generic)
     */
    public <C, R> Optional<R> callOptional(Class<C> clientClass,
                                           Function<C, String> clientCall,
                                           Class<R> responseType) {
        try {
            C client = applicationContext.getBean(clientClass);
            String raw = clientCall.apply(client);
            return parseOptional(raw, responseType);
        } catch (Exception ex) {
            log.warn("🔸 callOptional failed (Class): {}", ExceptionUtils.getStackTrace(ex));
            return Optional.empty();
        }
    }

    /**
     * Optional version of the above (generic)
     */
    public <C, R> Optional<R> callOptional(Class<C> clientClass,
                                           Function<C, String> clientCall,
                                           TypeReference<R> responseTypeRef) {
        try {
            C client = applicationContext.getBean(clientClass);
            String raw = clientCall.apply(client);
            return parseOptional(raw, responseTypeRef);
        } catch (Exception ex) {
            log.warn("🔸 callOptional failed (Generic): {}", ExceptionUtils.getStackTrace(ex));
            return Optional.empty();
        }
    }

    // ------------------ PARSE METHODS ------------------

    /**
     * Parses raw JSON to a non-generic type (e.g. MyDto).
     */
    public <T> T parse(String response, Class<T> responseType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response (Class): {}", apiResponseDto);
            if (apiResponseDto.isStatus()) {
                JsonNode objectNode = objectMapper.valueToTree(apiResponseDto.getObject());
                return objectMapper.treeToValue(objectNode, responseType);
            } else {
                throw exceptionComponent.expose("app.message.internal.api.failure", true);
            }
        } catch (Exception ex) {
            log.error("❌ Failed to parse response (Class): {}", ExceptionUtils.getStackTrace(ex));
            throw exceptionComponent.expose("app.message.internal.api.failure", true);
        }
    }

    /**
     * Parses raw JSON to a generic type (e.g. List<MyDto> or Map<String, Object>)
     */
    public <T> T parse(String response, TypeReference<T> typeRef) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(typeRef.getType());
            return parse(response, javaType);
        } catch (Exception ex) {
            log.error("❌ Failed to construct JavaType for parsing (TypeRef): {}", ExceptionUtils.getStackTrace(ex));
            throw exceptionComponent.expose("app.message.internal.api.failure", true);
        }
    }

    /**
     * Generic version using Jackson's JavaType for full control
     */
    public <T> T parse(String response, JavaType javaType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response (JavaType): {}", apiResponseDto);
            if (apiResponseDto.isStatus()) {
                return objectMapper.convertValue(apiResponseDto.getObject(), javaType);
            } else {
                throw exceptionComponent.expose("app.message.internal.api.failure", true);
            }
        } catch (Exception ex) {
            log.error("❌ Failed to parse response (JavaType): {}", ExceptionUtils.getStackTrace(ex));
            throw exceptionComponent.expose("app.message.internal.api.failure", true);
        }
    }

    // ------------------ OPTIONAL PARSE METHODS ------------------

    /**
     * Parses and returns an Optional for non-generic type.
     */
    public <T> Optional<T> parseOptional(String response, Class<T> responseType) {
        try {
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response (Optional/Class): {}", apiResponseDto);
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

    /**
     * Parses and returns an Optional for generic types.
     */
    public <T> Optional<T> parseOptional(String response, TypeReference<T> typeRef) {
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructType(typeRef.getType());
            APIResponseDto apiResponseDto = objectMapper.readValue(response, APIResponseDto.class);
            log.debug("✅ Parsed API response (Optional/Generic): {}", apiResponseDto);
            if (apiResponseDto.isStatus() && apiResponseDto.getObject() != null) {
                T result = objectMapper.convertValue(apiResponseDto.getObject(), javaType);
                return Optional.ofNullable(result);
            }
        } catch (Exception ex) {
            log.warn("🔸 Failed to parse optional response (Generic): {}", ExceptionUtils.getStackTrace(ex));
        }
        return Optional.empty();
    }

    // ------------------ SUPPLIER SHORTCUTS ------------------

    public <T> T parse(Supplier<String> supplier, Class<T> clazz) {
        return parse(supplier.get(), clazz);
    }

    public <T> T parse(Supplier<String> supplier, TypeReference<T> typeRef) {
        return parse(supplier.get(), typeRef);
    }
}
