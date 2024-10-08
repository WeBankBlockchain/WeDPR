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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.components.api.credential.core.impl.CredentialInfo;
import com.webank.wedpr.components.db.mapper.service.publish.model.PirSearchType;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.WeDPRException;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/** @author zachma */
@Data
public class PirQueryParam {
    // the credential information
    private CredentialInfo credentialInfo;

    private List<String> searchIdList;

    // the serviceId
    private String serviceId;

    // the queried fields
    private List<String> queriedFields;

    // the search type, default is search exist
    private String searchType = PirSearchType.SearchExist.getValue();
    @JsonIgnore private PirSearchType searchTypeObject;

    // the pir algorithm, default use id-filter algorithm
    private String pirAlgorithmType = PirParamEnum.AlgorithmType.idFilter.getValue();
    @JsonIgnore PirParamEnum.AlgorithmType algorithmType;
    // query range
    private Integer obfuscationOrder = 9;
    // the filter length
    private Integer filterLength = 4;

    public void generateCredentialInfo() {
        this.credentialInfo = new CredentialInfo();
    }

    public void setSearchType(String searchType) {
        if (StringUtils.isBlank(searchType)) {
            return;
        }
        this.searchType = searchType;
        this.searchTypeObject = PirSearchType.deserialize(searchType);
    }

    public void setPirAlgorithmType(String pirAlgorithmType) {
        if (StringUtils.isBlank(pirAlgorithmType)) {
            return;
        }
        this.pirAlgorithmType = pirAlgorithmType;
        this.algorithmType = PirParamEnum.AlgorithmType.deserialize(pirAlgorithmType);
    }

    public void check() throws WeDPRException {
        Common.requireNonNull("searchType", this.searchTypeObject);
        Common.requireNonNull("pirAlgorithmType", this.algorithmType);
        Common.requireNonNull("serviceID", serviceId);
        Common.requireNonNull("queriedFields", queriedFields);
        if (Objects.isNull(searchIdList) || searchIdList.size() == 0) {
            throw new WeDPRException(-1, "searchId列表不能为空");
        }
    }
}
