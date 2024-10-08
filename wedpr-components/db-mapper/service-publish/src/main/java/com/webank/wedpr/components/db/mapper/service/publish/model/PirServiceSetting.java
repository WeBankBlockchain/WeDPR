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

package com.webank.wedpr.components.db.mapper.service.publish.model;

import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/** @author zachma */
@Data
@ToString
public class PirServiceSetting {
    private String datasetId;
    private String idField;
    private String searchType;
    private PirSearchType searchTypeObject;
    private List<String> accessibleValueQueryFields;

    public List<String> obtainQueriedFields(PirSearchType searchType, List<String> queriedFields) {
        if (searchType == PirSearchType.SearchValue) {
            return (List<String>)
                    CollectionUtils.intersection(queriedFields, accessibleValueQueryFields);
        }
        return Collections.singletonList(idField);
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
        this.searchTypeObject = PirSearchType.deserialize(searchType);
    }

    public void setSearchTypeObject(PirSearchType searchTypeObject) {
        this.searchTypeObject = searchTypeObject;
        if (this.searchTypeObject != null) {
            this.searchType = this.searchTypeObject.getValue();
        }
    }

    public void check() throws Exception {
        Common.requireNonEmpty("datasetId", datasetId);
        Common.requireNonEmpty("idField", idField);
        Common.requireNonNull("searchType", searchTypeObject);
    }

    public static PirServiceSetting deserialize(String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            throw new WeDPRException("Invalid empty service config for pir");
        }
        return ObjectMapperFactory.getObjectMapper().readValue(data, PirServiceSetting.class);
    }
}
