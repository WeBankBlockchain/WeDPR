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
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.db.mapper.service.publish.model.PirServiceSetting;
import com.webank.wedpr.components.db.mapper.service.publish.model.ServiceStatus;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateData;
import com.webank.wedpr.components.pir.sdk.core.ObfuscateQueryResult;
import com.webank.wedpr.components.pir.sdk.core.OtResult;
import com.webank.wedpr.components.pir.sdk.model.PirQueryParam;
import com.webank.wedpr.components.pir.sdk.model.PirQueryRequest;
import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.components.task.plugin.pir.config.PirServiceConfig;
import com.webank.wedpr.components.task.plugin.pir.core.Obfuscator;
import com.webank.wedpr.components.task.plugin.pir.core.PirDatasetConstructor;
import com.webank.wedpr.components.task.plugin.pir.core.impl.ObfuscatorImpl;
import com.webank.wedpr.components.task.plugin.pir.core.impl.PirDatasetConstructorImpl;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapper;
import com.webank.wedpr.components.task.plugin.pir.dao.NativeSQLMapperWrapper;
import com.webank.wedpr.components.task.plugin.pir.model.ObfuscationParam;
import com.webank.wedpr.components.task.plugin.pir.model.PirDataItem;
import com.webank.wedpr.components.task.plugin.pir.service.PirService;
import com.webank.wedpr.components.task.plugin.pir.transport.PirTopicSubscriber;
import com.webank.wedpr.components.task.plugin.pir.transport.impl.PirTopicSubscriberImpl;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.ThreadPoolService;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PirServiceImpl implements PirService {
    private static final Logger logger = LoggerFactory.getLogger(PirServiceImpl.class);

    @Autowired private NativeSQLMapper nativeSQLMapper;
    @Autowired private DatasetMapper datasetMapper;
    @Autowired private HdfsStorageConfig hdfsConfig;
    @Autowired private LocalStorageConfig localStorageConfig;

    @Qualifier("fileStorage")
    @Autowired
    private FileStorageInterface fileStorage;

    @Qualifier("weDPRTransport")
    @Autowired
    private WeDPRTransport weDPRTransport;

    @Autowired private PublishedServiceMapper publishedServiceMapper;
    private final ThreadPoolService threadPoolService = PirServiceConfig.getThreadPoolService();

    private NativeSQLMapperWrapper nativeSQLMapperWrapper;
    private Obfuscator obfuscator;
    private PirDatasetConstructor pirDatasetConstructor;
    private PirTopicSubscriber pirTopicSubscriber;

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
        this.pirTopicSubscriber = new PirTopicSubscriberImpl(weDPRTransport);
    }

    @Override
    public WeDPRResponse query(PirQueryRequest pirQueryRequest) throws Exception {
        PublishedServiceInfo condition =
                new PublishedServiceInfo(pirQueryRequest.getQueryParam().getServiceId());
        // check the service
        List<PublishedServiceInfo> result =
                this.publishedServiceMapper.queryPublishedService(condition, null);
        if (result == null || result.isEmpty()) {
            throw new WeDPRException(
                    "The service "
                            + pirQueryRequest.getQueryParam().getServiceId()
                            + " not exists!");
        }
        // check the service status
        if (result.get(0).getServiceStatus() == null
                || !result.get(0).getServiceStatus().isReady()) {
            throw new WeDPRException(
                    "The service "
                            + pirQueryRequest.getQueryParam().getServiceId()
                            + " is not ready yet, status: "
                            + result.get(0).getStatus());
        }
        // TODO: check the auth
        // get the serviceSetting
        PirServiceSetting serviceSetting =
                PirServiceSetting.deserialize(result.get(0).getServiceConfig());
        return query(
                pirQueryRequest.getQueryParam(),
                serviceSetting,
                pirQueryRequest.getObfuscateData());
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
     * @param serviceSetting the serviceSetting
     * @return the result
     */
    @Override
    public WeDPRResponse publish(String serviceID, PirServiceSetting serviceSetting) {
        try {
            logger.info(
                    "Publish dataset: {}, serviceID: {}", serviceSetting.getDatasetId(), serviceID);
            serviceSetting.check();
            // Note: the publish operation maybe time-consuming, async here
            threadPoolService
                    .getThreadPool()
                    .execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    PublishedServiceInfo updatedInfo =
                                            new PublishedServiceInfo(serviceID);
                                    try {
                                        pirDatasetConstructor.construct(serviceSetting);
                                        pirTopicSubscriber.registerService(
                                                serviceID,
                                                new PirTopicSubscriber.QueryHandler() {
                                                    @Override
                                                    public WeDPRResponse onQuery(
                                                            PirQueryRequest pirQueryRequest)
                                                            throws Exception {
                                                        return query(pirQueryRequest);
                                                    }
                                                });
                                        updatedInfo.setStatus(
                                                ServiceStatus.PublishSuccess.getStatus());
                                        updatedInfo.setStatusMsg(Constant.WEDPR_SUCCESS_MSG);
                                        logger.info(
                                                "Publish dataset: {} success, serviceId: {}",
                                                serviceSetting.getDatasetId(),
                                                serviceID);
                                    } catch (Exception e) {
                                        logger.warn(
                                                "Publish failed, serviceId: {}, setting: {}, e: ",
                                                serviceID,
                                                serviceSetting.toString(),
                                                e);
                                        updatedInfo.setStatus(
                                                ServiceStatus.PublishFailed.getStatus());
                                        updatedInfo.setStatusMsg(
                                                "Publish PIR service "
                                                        + serviceID
                                                        + " failed for "
                                                        + e.getMessage());
                                    }
                                    publishedServiceMapper.updateServiceInfo(updatedInfo);
                                }
                            });

            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } catch (Exception e) {
            logger.warn(
                    "publish dataset {} failed, serviceID: {}, error: ",
                    serviceSetting.getDatasetId(),
                    serviceID,
                    e);
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, "Publish dataset {} failed for " + e.getMessage());
        }
    }
}
