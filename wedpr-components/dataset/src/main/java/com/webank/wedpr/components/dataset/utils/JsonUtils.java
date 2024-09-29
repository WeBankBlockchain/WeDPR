package com.webank.wedpr.components.dataset.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.core.utils.ObjectMapperFactory;

public class JsonUtils {

    private JsonUtils() {}

    public static <T> Object jsonString2Object(String jsonStr, Class<T> cls)
            throws DatasetException {
        try {
            return ObjectMapperFactory.getObjectMapper().readValue(jsonStr, cls);

        } catch (JsonProcessingException e) {
            throw new DatasetException("Invalid json object format, e: " + e.getMessage());
        }
    }
}
