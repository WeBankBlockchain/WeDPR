/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.common.utils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.commons.collections.MapUtils;

public class PropertiesHelper {

    @SneakyThrows
    public static <T> T getValue(
            Map<String, String> config, String key, boolean required, T defaultValue) {
        return getValue(MapUtils.toProperties(config), key, required, defaultValue);
    }

    @SneakyThrows
    public static <T> T getValue(Properties config, String key, boolean required, T defaultValue) {
        if (config == null) {
            if (!required) {
                return defaultValue;
            }
            return null;
        }
        String value = config.getProperty(key);
        if (required && value == null) {
            throw new WeDPRException("The configuration of '" + key + "' is required!");
        }
        if (value == null) {
            config.setProperty(key, String.valueOf(defaultValue));
            return defaultValue;
        }
        return convert(value, defaultValue);
    }

    private static <T> T convert(String value, T defaultValue) {
        if (value == null) {
            return null;
        }
        if (defaultValue instanceof String) {
            return (T) value;
        }
        if (defaultValue instanceof byte[]) {
            return (T) value.getBytes(StandardCharsets.UTF_8);
        }
        if (defaultValue instanceof Short) {
            return (T) Short.valueOf(value);
        }
        if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(value);
        }
        if (defaultValue instanceof Long) {
            return (T) Long.valueOf(value);
        }
        if (defaultValue instanceof Float) {
            return (T) Float.valueOf(value);
        }
        if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(value);
        }
        return (T) value;
    }
}
