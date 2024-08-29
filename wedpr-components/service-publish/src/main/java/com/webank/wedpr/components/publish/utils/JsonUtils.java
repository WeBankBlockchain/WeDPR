package com.webank.wedpr.components.publish.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.webank.wedpr.components.dataset.exception.DatasetException;

/**
 * @author zachma
 * @date 2024/8/29
 */
public class JsonUtils {
    private JsonUtils() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public static <T> T jsonString2Object(String jsonStr, Class<T> cls) throws DatasetException {
        try {
            return OBJECT_MAPPER.readValue(jsonStr, cls);
        } catch (JsonProcessingException e) {
            throw new DatasetException("Invalid json object format, e: " + e.getMessage());
        }
    }

    public static String object2jsonString(Object cls) throws DatasetException {
        try {
            return OBJECT_MAPPER.writeValueAsString(cls);
        } catch (JsonProcessingException e) {
            throw new DatasetException("Invalid json object format, e: " + e.getMessage());
        }
    }
}
