package org.example.movieanalytics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JsonService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new RuntimeException("Ошибка сериализации JSON", e); }
    }
    public Map<String, Integer> mapFromJson(String json) {
        if (json == null || json.isBlank()) return new LinkedHashMap<>();
        try { return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Integer>>() {}); }
        catch (Exception e) { return new LinkedHashMap<>(); }
    }
}
