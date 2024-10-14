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

package com.webank.wedpr.components.pir.sdk.model;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.CSVFileParser;
import com.webank.wedpr.common.utils.FileUtils;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.db.mapper.service.publish.model.PirSearchType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author zachma */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PirResult {
    private static final Logger logger = LoggerFactory.getLogger(PirResult.class);

    @Data
    @NoArgsConstructor
    @ToString
    public static class PirResultItem {
        String searchId;
        Boolean isExists = false;
        List<Map<String, String>> values = new ArrayList<>();

        public boolean appendValueData(PirSearchType pirSearchType, String value) {
            if (StringUtils.isNotBlank(value)) {
                try {
                    Map<String, String> queriedData =
                            ObjectMapperFactory.getObjectMapper().readValue(value, Map.class);
                    if (pirSearchType == PirSearchType.SearchValue) {
                        values.add(queriedData);
                    }
                    this.isExists = true;
                } catch (Exception e) {
                    this.isExists = false;
                }
            } else {
                this.isExists = false;
            }
            return this.isExists;
        }

        public void setValues(List<Map<String, String>> values) {
            if (values == null) {
                return;
            }
            this.values = values;
        }
    }

    private String searchType;
    private List<PirResultItem> pirResultItemList;

    public boolean persistentResult(String filePath) throws Exception {
        if (pirResultItemList == null || pirResultItemList.isEmpty()) {
            return false;
        }
        if (searchType.compareToIgnoreCase(PirSearchType.SearchValue.getValue()) == 0) {
            persistentValue(filePath);
        } else {
            persistentExistenceResult(filePath);
        }
        return true;
    }

    private String getExistenceDesc(boolean existence) {
        return (existence ? "Yes" : "No");
    }

    private void persistentExistenceResult(String filePath) throws Exception {
        FileUtils.createParentDirectory(Paths.get(filePath));
        try (Writer writer =
                new BufferedWriter(
                        new FileWriter(filePath), WeDPRCommonConfig.getWriteChunkSize())) {
            // write the headers
            writer.write("SearchId,Existence\n");
            // write the values
            for (PirResultItem item : pirResultItemList) {
                writer.write(
                        item.getSearchId() + "," + getExistenceDesc(item.getIsExists()) + "\n");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void persistentValue(String filePath) throws Exception {
        CSVFileParser.writeMapData(pirResultItemList.get(0).getValues(), false, filePath);
        for (int i = 1; i < pirResultItemList.size(); i++) {
            CSVFileParser.writeMapData(pirResultItemList.get(i).getValues(), true, filePath);
        }
    }

    @Override
    public String toString() {
        return "PirResult{" + "pirResultItemList=" + ArrayUtils.toString(pirResultItemList) + '}';
    }
}
