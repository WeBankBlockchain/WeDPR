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

package com.webank.wedpr.components.task.plugin.pir.model;

import com.webank.wedpr.components.pir.sdk.model.PirParamEnum;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * @author zachma
 * @date 2024/9/3
 */
@Data
public class PirServiceSetting {
    private String datasetId;
    private String idField;
    private List<String> accessibleExistenceQueryFields;
    private List<String> accessibleValueQueryFields;

    public List<String> obtainQueriedFields(
            PirParamEnum.SearchType searchType, List<String> queriedFields) {
        if (searchType == PirParamEnum.SearchType.SearchExist) {
            return (List<String>)
                    CollectionUtils.intersection(queriedFields, accessibleExistenceQueryFields);
        }
        return (List<String>)
                CollectionUtils.intersection(queriedFields, accessibleValueQueryFields);
    }
}
