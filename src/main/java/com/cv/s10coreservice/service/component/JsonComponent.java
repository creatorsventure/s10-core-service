package com.cv.s10coreservice.service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonComponent {

    private final ObjectMapper objectMapper; // Injected from Spring Boot context

    public String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    public <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
