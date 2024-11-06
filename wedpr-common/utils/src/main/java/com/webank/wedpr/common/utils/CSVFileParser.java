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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.CSVReaderHeaderAware;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVFileParser {
    private static final Logger logger = LoggerFactory.getLogger(CSVFileParser.class);

    private interface ParseHandler {
        Object call(CSVReaderHeaderAware reader) throws Exception;
    }

    private static Object loadCSVFile(String filePath, int chunkSize, ParseHandler handler)
            throws Exception {
        try (Reader fileReader = new BufferedReader(new FileReader(filePath), chunkSize);
                CSVReaderHeaderAware reader = new CSVReaderHeaderAware(fileReader)) {
            if (handler != null) {
                return handler.call(reader);
            }
            return null;
        } catch (Exception e) {
            logger.warn("CSVFileParser exception, filePath: {}, error: ", filePath, e);
            throw new WeDPRException("loadCSVFile exception for " + e.getMessage(), e);
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractConfig {
        private String originalFilePath;
        private List<String> extractFields;
        private String extractFilePath;
        private String fieldSplitter = WeDPRCommonConfig.getFieldSplitter();
        private Integer writeChunkSize = WeDPRCommonConfig.getWriteChunkSize();
        private Integer readChunkSize = WeDPRCommonConfig.getReadChunkSize();

        public ExtractConfig(
                String originalFilePath, List<String> extractFields, String extractFilePath) {
            this.originalFilePath = originalFilePath;
            this.extractFields = extractFields;
            this.extractFilePath = extractFilePath;
        }

        @Override
        public String toString() {
            return "ExtractConfig{"
                    + "originalFilePath='"
                    + originalFilePath
                    + '\''
                    + ", extractFields="
                    + extractFields
                    + ", extractFilePath='"
                    + extractFilePath
                    + '\''
                    + ", fieldSplitter='"
                    + fieldSplitter
                    + '\''
                    + ", writeChunkSize="
                    + writeChunkSize
                    + ", readChunkSize="
                    + readChunkSize
                    + '}';
        }
    }

    public static void extractFields(ExtractConfig extractConfig) throws Exception {
        loadCSVFile(
                extractConfig.getOriginalFilePath(),
                extractConfig.getReadChunkSize(),
                new ParseHandler() {
                    @Override
                    public Object call(CSVReaderHeaderAware reader) throws Exception {
                        // check the fields
                        Map<String, String> headerInfo = reader.readMap();
                        Map<String, String> fieldsMapping =
                                Common.trimAndMapping(headerInfo.keySet());
                        for (String field : extractConfig.getExtractFields()) {
                            if (!fieldsMapping.containsKey(field.trim())) {
                                String errorMsg =
                                        "extractFields failed for the field "
                                                + field
                                                + " not existed in the file "
                                                + extractConfig.getOriginalFilePath();
                                logger.warn(errorMsg);
                                throw new WeDPRException(errorMsg);
                            }
                        }
                        Map<String, String> row;
                        try (Writer writer =
                                new BufferedWriter(
                                        new FileWriter(extractConfig.getExtractFilePath()),
                                        extractConfig.getWriteChunkSize())) {
                            // write the data(Note: here no need to write the header)
                            writer.write(
                                    Constant.DEFAULT_ID_FIELD + Constant.DEFAULT_LINE_SPLITTER);
                            while ((row = reader.readMap()) != null) {
                                int column = 0;
                                for (String field : extractConfig.getExtractFields()) {
                                    // Note: the key for row maybe exist blanks
                                    writer.write(row.get(fieldsMapping.get(field)));
                                    if (column < extractConfig.getExtractFields().size() - 1) {
                                        writer.write(extractConfig.getFieldSplitter());
                                    }
                                    column++;
                                }
                                writer.write(Constant.DEFAULT_LINE_SPLITTER);
                            }
                        } catch (Exception e) {
                            logger.warn(
                                    "extractFields exception, config: {}, error",
                                    extractConfig.toString(),
                                    e);
                            throw new WeDPRException(
                                    "extractFields exception, detail: "
                                            + extractConfig.toString()
                                            + ", error: "
                                            + e.getMessage(),
                                    e);
                        }
                        return null;
                    }
                });
    }

    public interface RowContentHandler {
        void handle(List<String> rowContent) throws Exception;
    }

    public static void processCsvContent(
            String[] tableFields, String csvFilePath, RowContentHandler rowContentHandler)
            throws Exception {
        loadCSVFile(
                csvFilePath,
                WeDPRCommonConfig.getReadChunkSize(),
                reader -> {
                    Map<String, String> row;
                    while ((row = reader.readMap()) != null) {
                        List<String> rowValue = new ArrayList<>();
                        for (String field : tableFields) {
                            Map<String, String> rowFieldsMapping =
                                    Common.trimAndMapping(row.keySet());
                            if (!rowFieldsMapping.containsKey(field.trim())) {
                                String errorMsg =
                                        "extractFields failed for the field "
                                                + field
                                                + " not existed in the file "
                                                + ArrayUtils.toString(rowFieldsMapping.keySet());
                                logger.warn(errorMsg);
                                throw new WeDPRException(-1, errorMsg);
                            }
                            rowValue.add(row.get(rowFieldsMapping.get(field)));
                        }
                        rowContentHandler.handle(rowValue);
                    }
                    return Boolean.TRUE;
                });
    }

    public static boolean writeMapData(
            List<Map<String, String>> mapObjects, Boolean append, String generatedFilePath)
            throws Exception {
        if (mapObjects == null || mapObjects.isEmpty()) {
            return false;
        }
        FileUtils.createParentDirectory(Paths.get(generatedFilePath));
        try (Writer writer =
                new BufferedWriter(
                        new FileWriter(generatedFilePath, append),
                        WeDPRCommonConfig.getWriteChunkSize())) {
            // write the headers
            if (!append) {
                writer.write(String.join(",", mapObjects.get(0).keySet()) + "\n");
            }
            // write the values
            for (Map<String, String> item : mapObjects) {
                writer.write(String.join(",", item.values()) + "\n");
            }
            return true;
        } catch (Exception e) {
            logger.warn("mapStr2Json exception, error: ", e);
            throw e;
        }
    }
}
