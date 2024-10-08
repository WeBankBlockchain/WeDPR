package com.webank.wedpr.components.publish.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.publish.entity.request.PublishCreateRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishCreateResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchResponse;
import com.webank.wedpr.components.publish.service.WedprPublishedServiceService;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.components.publish.sync.api.PublishSyncerApi;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-08-31
 */
@Service
public class WedprPublishedServiceServiceImpl implements WedprPublishedServiceService {

    private static final Logger logger =
            LoggerFactory.getLogger(WedprPublishedServiceServiceImpl.class);

    @Qualifier("publishSyncer")
    @Autowired
    private PublishSyncerApi publishSyncer;

    @Autowired
    @Qualifier("datasetAsyncExecutor")
    private Executor executor;

    @Autowired private DatasetMapper datasetMapper;
    @Autowired private PublishedServiceMapper publishedServiceMapper;

    @Override
    @Transactional(rollbackFor = WeDPRException.class)
    public WeDPRResponse createPublishService(String username, PublishCreateRequest publishCreate)
            throws Exception {
        publishCreate.setAgency(WeDPRCommonConfig.getAgency());
        publishCreate.checkServiceConfig(datasetMapper);
        this.publishedServiceMapper.insertServiceInfo(publishCreate);
        publishSyncer.publishSync(publishCreate.serialize());
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishCreateResponse(publishCreate.getServiceId()));
    }

    @Override
    public WeDPRResponse updatePublishService(
            String username, PublishedServiceInfo publishedServiceInfo) throws Exception {
        Common.requireNonEmpty("serviceId", publishedServiceInfo.getServiceId());
        publishedServiceInfo.setOwner(username);
        publishedServiceInfo.setAgency(WeDPRCommonConfig.getAgency());
        Integer result = this.publishedServiceMapper.updateServiceInfo(publishedServiceInfo);
        if (result != null && result > 0) {
            publishSyncer.publishSync(publishedServiceInfo.serialize());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } else {
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, publishedServiceInfo.getServiceId() + "服务更新失败");
        }
    }

    @Override
    public WeDPRResponse revokePublishService(String username, String serviceId) throws Exception {
        Integer result =
                this.publishedServiceMapper.deleteServiceInfo(
                        serviceId, username, WeDPRCommonConfig.getAgency());
        if (result != null && result > 0) {
            PublishedServiceInfo publishedServiceInfo = new PublishedServiceInfo();
            publishedServiceInfo.setServiceId(serviceId);
            publishedServiceInfo.setOwner(username);
            publishedServiceInfo.setAgency(WeDPRCommonConfig.getAgency());
            publishSyncer.revokeSync(publishedServiceInfo.serialize());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } else {
            return new WeDPRResponse(Constant.WEDPR_FAILED, serviceId + "服务撤回失败");
        }
    }

    @Override
    public WeDPRResponse listPublishService(PublishSearchRequest request) {
        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(request)) {
            List<PublishedServiceInfo> result =
                    this.publishedServiceMapper.queryPublishedService(request.getCondition());
            WedprPublishSearchResponse response =
                    new WedprPublishSearchResponse(
                            new PageInfo<PublishedServiceInfo>(result).getTotal(), result);
            weDPRResponse.setData(response);
            return weDPRResponse;
        } catch (Exception e) {
            logger.warn("listPublishService exception, request: {}, e: ", request.toString(), e);
            return new WeDPRResponse(Constant.WEDPR_FAILED, e.getMessage());
        }
    }

    @Override
    public void syncPublishService(
            PublishSyncAction action, PublishedServiceInfo publishedServiceInfo) {
        List<PublishedServiceInfo> existServiceList =
                this.publishedServiceMapper.queryPublishedService(publishedServiceInfo);
        if (action == PublishSyncAction.SYNC) {
            if (existServiceList == null || existServiceList.isEmpty()) {
                this.publishedServiceMapper.insertServiceInfo(publishedServiceInfo);
            } else {
                this.publishedServiceMapper.updateServiceInfo(publishedServiceInfo);
            }
        }

        if (action == PublishSyncAction.REVOKE) {
            this.publishedServiceMapper.deleteServiceInfo(
                    publishedServiceInfo.getServiceId(),
                    publishedServiceInfo.getOwner(),
                    publishedServiceInfo.getAgency());
        }
    }
}
