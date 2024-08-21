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
package com.webank.wedpr.core.utils;

import static com.webank.wedpr.core.utils.Constant.DEFAULT_DATE_FORMAT;
import static com.webank.wedpr.core.utils.Constant.DEFAULT_TIMESTAMP_FORMAT;

import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

public class Common {
    @SneakyThrows(WeDPRException.class)
    public static void requireNonEmpty(String fieldName, String fieldValue) {
        if (StringUtils.isBlank(fieldValue)
                || fieldValue.compareToIgnoreCase(Constant.DEFAULT_NULL_STR) == 0) {
            throw new WeDPRException("The field " + fieldName + " must be non-empty!");
        }
    }

    @SneakyThrows(WeDPRException.class)
    public static void requireEmpty(String fieldName, String fieldValue, String reason) {
        if (StringUtils.isBlank(fieldValue)
                || fieldValue.compareToIgnoreCase(Constant.DEFAULT_NULL_STR) == 0) {
            return;
        }
        throw new WeDPRException(
                "The field " + fieldName + " must be empty for reason: " + reason + "!");
    }

    public static Map<String, Object> objectToMap(Object object) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            result.put(field.getName(), field.get(object));
        }
        return result;
    }

    @SneakyThrows(WeDPRException.class)
    public static void requireNonNull(String fieldName, Object fieldValue) {
        if (Objects.isNull(fieldValue)) {
            throw new WeDPRException("The field " + fieldName + " must be non-null!");
        }
    }

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    public static String joinPath(String filePath, String file) {
        return filePath + File.separator + file;
    }

    public static boolean deleteFile(File file) {
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    public static String getCron(Integer value) {

        if (value < 60) {
            return "0/" + value + " * * * * ?";
        } else if (value < 3600) {
            return "0 0/" + value / 60 + " * * * ?";
        } else if (value < 86400) {
            return "0 0 0/" + value / 60 + " * * ?";
        } else {
            return "0 0 0 * * ?";
        }
    }

    public static boolean isNullStr(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        if (str.compareToIgnoreCase(Constant.DEFAULT_NULL_STR) == 0) {
            return true;
        }
        return false;
    }

    public static LocalDateTime toDate(String time) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return LocalDate.parse(time, formatter).atStartOfDay();
    }

    public static String dateToString(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        return time.format(formatter);
    }

    public static String timeToString(LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_TIMESTAMP_FORMAT);
        return time.format(formatter);
    }

    public static String getCurrentTime() {
        return timeToString(LocalDateTime.now());
    }
}
