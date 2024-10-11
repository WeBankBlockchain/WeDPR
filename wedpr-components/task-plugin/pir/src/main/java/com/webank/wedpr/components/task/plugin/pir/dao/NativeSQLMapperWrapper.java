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

import com.webank.wedpr.components.db.mapper.service.publish.model.PirServiceSetting;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.task.plugin.pir.model.PirDataItem;
import com.webank.wedpr.components.task.plugin.pir.utils.Constant;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            PirServiceSetting serviceSetting,
            PirQueryParam queryParam,
            ObfuscateData.ObfuscateDataItem obfuscateDataItem)
            throws Exception {
        List<String> queriedFields =
                serviceSetting.obtainQueriedFields(
                        queryParam.getSearchTypeObject(), queryParam.getQueriedFields());
        logger.trace(
                "query, origin queriedFields: [{}], intersection fields: [{}]",
                StringUtils.join(queryParam.getQueriedFields(), ","),
                StringUtils.join(queriedFields, ","));
        String tableName = Constant.datasetId2tableId(serviceSetting.getDatasetId());
        if (queryParam.getAlgorithmType() == PirParamEnum.AlgorithmType.idFilter) {
            return executeFuzzyMatchQuery(
                    tableName, serviceSetting, queriedFields, obfuscateDataItem.getFilter());
        }
        if (queryParam.getAlgorithmType() == PirParamEnum.AlgorithmType.idObfuscation) {
            return executeQuery(
                    tableName, serviceSetting, queriedFields, obfuscateDataItem.getIdHashList());
        }
        throw new WeDPRException(
                "query "
                        + serviceSetting.getDatasetId()
                        + " failed for encounter unsupported algorithmType: "
                        + queryParam.getAlgorithmType().getValue());
    }

    public List<PirDataItem> executeQuery(
            String tableName,
            PirServiceSetting serviceSetting,
            List<String> queriedFields,
            List<String> filters)
            throws Exception {
        String condition =
                String.format(
                        "where t.%s in (%s)",
                        Constant.ID_HASH_FIELD_NAME, Common.joinAndAddDoubleQuotes(filters));
        String sql =
                String.format(
                        "select t.%s, %s from %s t %s",
                        Constant.ID_HASH_FIELD_NAME,
                        StringUtils.join(queriedFields, ","),
                        tableName,
                        condition);
        logger.debug("executeQuery: {}", sql);
        return toPirDataList(this.nativeSQLMapper.executeNativeQuerySql(sql));
    }

    public List<PirDataItem> executeFuzzyMatchQuery(
            String tableName,
            PirServiceSetting serviceSetting,
            List<String> queriedFields,
            String filter)
            throws Exception {
        String condition =
                String.format(
                        "where t.%s like concat('%s', '%%')", Constant.ID_HASH_FIELD_NAME, filter);
        String sql =
                String.format(
                        "select t.%s, %s from %s t %s",
                        Constant.ID_HASH_FIELD_NAME,
                        StringUtils.join(queriedFields, ","),
                        tableName,
                        condition);
        logger.debug("executeQuery: {}", sql);
        return toPirDataList(this.nativeSQLMapper.executeNativeQuerySql(sql));
    }

    protected static List<PirDataItem> toPirDataList(List<Map<String, Object>> values)
            throws Exception {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<PirDataItem> result = new LinkedList<>();
        int i = 0;
        for (Map<String, Object> row : values) {
            PirDataItem pirTable = new PirDataItem();
            pirTable.setId(i);
            // the key, Note: here must use the idField value since the client use the idField value
            // to calculateZ0
            pirTable.setPirKey((String) row.get(Constant.ID_FIELD_NAME));
            // the values
            pirTable.setPirValue(ObjectMapperFactory.getObjectMapper().writeValueAsString(row));
            logger.trace("toPirDataList result: {}", pirTable.toString());
            result.add(pirTable);
            i++;
        }
        return result;
    }
}
