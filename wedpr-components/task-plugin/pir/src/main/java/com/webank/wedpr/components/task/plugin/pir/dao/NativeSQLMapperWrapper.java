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

package com.webank.wedpr.components.task.plugin.pir.dao;

import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import com.webank.wedpr.components.task.plugin.pir.model.PirDataItem;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeSQLMapperWrapper {
    private static Logger logger = LoggerFactory.getLogger(NativeSQLMapperWrapper.class);
    private final NativeSQLMapper nativeSQLMapper;

    public NativeSQLMapperWrapper(NativeSQLMapper nativeSQLMapper) {
        this.nativeSQLMapper = nativeSQLMapper;
    }

    public List<PirDataItem> query(
            PirParamEnum.AlgorithmType algorithmType,
            String datasetID,
            String[] params,
            ObfuscateData.ObfuscateDataItem obfuscateDataItem)
            throws WeDPRException {
        if (algorithmType == PirParamEnum.AlgorithmType.idFilter) {
            return executeFuzzyMatchQuery(datasetID, obfuscateDataItem.getFilter(), params);
        }
        if (algorithmType == PirParamEnum.AlgorithmType.idObfuscation) {
            return executeQuery(datasetID, obfuscateDataItem.getIdHashList(), params);
        }
        throw new WeDPRException(
                "query "
                        + datasetID
                        + " failed for encounter unsupported algorithmType: "
                        + algorithmType.getValue());
    }

    public List<PirDataItem> executeQuery(String datasetID, List<String> filters, String[] params) {
        String condition = String.format("where t.id in (%s)", StringUtils.join(filters, ","));
        String sql =
                String.format(
                        "select t.id as t_id, %s from %s t %s",
                        StringUtils.join(params, ","), datasetID, condition);
        logger.debug("executeQuery: {}", sql);
        return toPirDataList(this.nativeSQLMapper.executeNativeQuerySql(sql));
    }

    public List<PirDataItem> executeFuzzyMatchQuery(
            String datasetID, String filter, String[] params) {
        String condition = String.format("where t.id like concat(%s, '%%')", filter);
        String sql =
                String.format(
                        "select t.id as t_id, %s from %s t %s",
                        StringUtils.join(params, ","), datasetID, condition);
        logger.debug("executeQuery: {}", sql);
        return toPirDataList(this.nativeSQLMapper.executeNativeQuerySql(sql));
    }

    protected static List<PirDataItem> toPirDataList(List values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<PirDataItem> result = new LinkedList<>();
        for (int i = 0; i < values.size(); i++) {
            Object[] temp = (Object[]) values.get(i);
            PirDataItem pirTable = new PirDataItem();
            pirTable.setId(i + 1);
            pirTable.setPirKey(String.valueOf(temp[0]));
            Object[] objects = Arrays.copyOfRange(temp, 1, temp.length);
            pirTable.setPirValue(Arrays.toString(objects));
            result.add(pirTable);
        }
        return result;
    }
}
