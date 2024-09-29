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

import com.opencsv.CSVReaderHeaderAware;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static Set<String> getFields(String filePath) throws Exception {
        return (Set<String>)
                (loadCSVFile(
                        filePath,
                        WeDPRCommonConfig.getReadChunkSize(),
                        new ParseHandler() {
                            @Override
                            public Object call(CSVReaderHeaderAware reader) throws Exception {
                                return reader.readMap().keySet();
                            }
                        }));
    }

    public static class ExtractConfig {
        private String originalFilePath;
        private List<String> extractFields;
        private String extractFilePath;
        private String fieldSplitter = WeDPRCommonConfig.getFieldSplitter();
        private Integer writeChunkSize = WeDPRCommonConfig.getWriteChunkSize();
        private Integer readChunkSize = WeDPRCommonConfig.getReadChunkSize();

        public ExtractConfig() {}

        public ExtractConfig(
                String originalFilePath, List<String> extractFields, String extractFilePath) {
            this.originalFilePath = originalFilePath;
            this.extractFields = extractFields;
            this.extractFilePath = extractFilePath;
        }

        public String getOriginalFilePath() {
            return originalFilePath;
        }

        public void setOriginalFilePath(String originalFilePath) {
            this.originalFilePath = originalFilePath;
        }

        public List<String> getExtractFields() {
            return extractFields;
        }

        public void setExtractFields(List<String> extractFields) {
            this.extractFields = extractFields;
        }

        public String getExtractFilePath() {
            return extractFilePath;
        }

        public void setExtractFilePath(String extractFilePath) {
            this.extractFilePath = extractFilePath;
        }

        public String getFieldSplitter() {
            return fieldSplitter;
        }

        public void setFieldSplitter(String fieldSplitter) {
            this.fieldSplitter = fieldSplitter;
        }

        public Integer getWriteChunkSize() {
            return writeChunkSize;
        }

        public void setWriteChunkSize(Integer writeChunkSize) {
            this.writeChunkSize = writeChunkSize;
        }

        public Integer getReadChunkSize() {
            return readChunkSize;
        }

        public void setReadChunkSize(Integer readChunkSize) {
            this.readChunkSize = readChunkSize;
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
                        Set<String> fields = headerInfo.keySet();
                        for (String field : extractConfig.getExtractFields()) {
                            if (!fields.contains(field.trim())) {
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
                            while ((row = reader.readMap()) != null) {
                                int column = 0;
                                for (String field : extractConfig.getExtractFields()) {
                                    writer.write(row.get(field));
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

    public static List<List<String>> processCsv2SqlMap(String[] tableFields, String csvFilePath)
            throws Exception {
        return (List<List<String>>)
                loadCSVFile(
                        csvFilePath,
                        WeDPRCommonConfig.getReadChunkSize(),
                        reader -> {
                            List<List<String>> resultValue = new ArrayList<>();
                            Map<String, String> row;
                            while ((row = reader.readMap()) != null) {
                                List<String> rowValue = new ArrayList<>();
                                for (String field : tableFields) {
                                    if (!row.keySet().contains(field.trim())) {
                                        String errorMsg =
                                                "extractFields failed for the field "
                                                        + field
                                                        + " not existed in the file "
                                                        + tableFields.toString();
                                        logger.warn(errorMsg);
                                        throw new WeDPRException(-1, errorMsg);
                                    }
                                    rowValue.add(row.get(field));
                                }
                                resultValue.add(rowValue);
                            }
                            return resultValue;
                        });
    }
}
