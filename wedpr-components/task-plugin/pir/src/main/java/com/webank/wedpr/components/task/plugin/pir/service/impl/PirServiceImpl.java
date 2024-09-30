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

package com.webank.wedpr.components.task.plugin.pir.service.impl;

import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateQueryResult;
import com.webank.wedpr.components.pir.sdk.core.OtResult;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.task.plugin.pir.core.Obfuscator;
import com.webank.wedpr.components.task.plugin.pir.core.PirDatasetConstructor;
import com.webank.wedpr.components.task.plugin.pir.core.impl.ObfuscatorImpl;
import com.webank.wedpr.components.task.plugin.pir.core.impl.PirDatasetConstructorImpl;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapper;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapperWrapper;
import com.webank.wedpr.components.task.plugin.pir.model.ObfuscationParam;
import com.webank.wedpr.components.task.plugin.pir.model.PirDataItem;
import com.webank.wedpr.components.task.plugin.pir.model.PirServiceSetting;
import com.webank.wedpr.components.task.plugin.pir.service.PirService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PirServiceImpl implements PirService {
    private static Logger logger = LoggerFactory.getLogger(PirServiceImpl.class);

    private @Autowired NativeSQLMapper nativeSQLMapper;
    private @Autowired DatasetMapper datasetMapper;
    @Autowired private HdfsStorageConfig hdfsConfig;
    @Autowired private LocalStorageConfig localStorageConfig;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    private NativeSQLMapperWrapper nativeSQLMapperWrapper;

    private Obfuscator obfuscator;
    private PirDatasetConstructor pirDatasetConstructor;

    @PostConstruct
    public void init() {
        this.obfuscator = new ObfuscatorImpl();
        this.nativeSQLMapperWrapper = new NativeSQLMapperWrapper(nativeSQLMapper);
        this.pirDatasetConstructor =
                new PirDatasetConstructorImpl(
                        datasetMapper,
                        fileStorage,
                        new StoragePathBuilder(hdfsConfig, localStorageConfig),
                        nativeSQLMapper);
    }

    @Override
    public WeDPRResponse query(PirQueryRequest pirQueryRequest) {
        // TODO: obtain the serviceSetting
        return query(pirQueryRequest.getQueryParam(), null, pirQueryRequest.getObfuscateData());
    }

    /**
     * query the data
     *
     * @param obfuscateData the query parm
     * @return the result
     */
    protected WeDPRResponse query(
            PirQueryParam pirQueryParam,
            PirServiceSetting serviceSetting,
            ObfuscateData obfuscateData) {
        try {
            ObfuscationParam obfuscationParam =
                    new ObfuscationParam(obfuscateData, pirQueryParam.getAlgorithmType());
            ObfuscateQueryResult obfuscateQueryResult =
                    new ObfuscateQueryResult(
                            serviceSetting.getDatasetId(),
                            pirQueryParam.getAlgorithmType().toString());
            for (ObfuscateData.ObfuscateDataItem dataItem : obfuscateData.getObfuscateDataItems()) {
                List<PirDataItem> queriedResult =
                        this.nativeSQLMapperWrapper.query(serviceSetting, pirQueryParam, dataItem);
                obfuscationParam.setIndex(dataItem.getIdIndex());
                List<OtResult.OtResultItem> otResultItems =
                        this.obfuscator.obfuscate(obfuscationParam, queriedResult, dataItem);
                obfuscateQueryResult.getOtResultList().add(new OtResult(otResultItems));
            }
            WeDPRResponse response =
                    new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
            response.setData(obfuscateQueryResult);
            return response;
        } catch (Exception e) {
            logger.warn(
                    "query exception, dataset: {}, queryParam: {}, e: ",
                    serviceSetting.getDatasetId(),
                    pirQueryParam.toString(),
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED,
                    "Pir query failed for "
                            + e.getMessage()
                            + ", datasetID: "
                            + serviceSetting.getDatasetId());
        }
    }

    /**
     * publish pir service
     *
     * @param datasetID the datasetID
     * @return the result
     */
    @Override
    public WeDPRResponse publish(String datasetID) {
        try {
            logger.info("Publish dataset: {}", datasetID);
            this.pirDatasetConstructor.construct(datasetID);
            logger.info("Publish dataset: {} success", datasetID);
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } catch (Exception e) {
            logger.warn("publish dataset {} failed for ", datasetID, e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "Publish dataset {} failed for " + e.getMessage());
        }
    }
}
