package com.webank.wedpr.components.publish.service.impl;

import com.github.pagehelper.PageInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceMapper;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeDO;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeMapper;
import com.webank.wedpr.components.mybatis.PageHelperWrapper;
import com.webank.wedpr.components.publish.entity.request.PublishInvokeSearchRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishInvokeSearchResponse;
import com.webank.wedpr.components.publish.service.WedprServiceInvokeTableService;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-08-31
 */
@Service
public class WedprServiceInvokeTableServiceImpl implements WedprServiceInvokeTableService {
    private static final Logger logger =
            LoggerFactory.getLogger(WedprServiceInvokeTableServiceImpl.class);

    @Autowired private PublishedServiceMapper publishedServiceMapper;
    @Autowired private ServiceInvokeMapper serviceInvokeMapper;

    @Override
    public WeDPRResponse seachPublishInvokeService(
            String user, String agency, PublishInvokeSearchRequest publishInvokeRequest)
            throws Exception {
        // check the service
        publishInvokeRequest.check();
        PublishedServiceInfo condition =
                new PublishedServiceInfo(publishInvokeRequest.getCondition().getServiceId());
        condition.setOwner(user);
        condition.setAgency(agency);
        List<PublishedServiceInfo> serviceInfos =
                publishedServiceMapper.queryPublishedService(condition, null);
        if (serviceInfos == null || serviceInfos.isEmpty()) {
            logger.warn(
                    "seachPublishInvokeService failed, only the owner can invoke seachPublishInvokeService, currentUser: {}, currentAgency: {}, service: {}",
                    user,
                    agency,
                    publishInvokeRequest.getCondition().getServiceId());
            throw new WeDPRException("Only the owner can search the invoke recorders!");
        }
        // query the recorders
        try (PageHelperWrapper pageHelperWrapper = new PageHelperWrapper(publishInvokeRequest)) {
            List<ServiceInvokeDO> invokeDOList =
                    serviceInvokeMapper.query(publishInvokeRequest.getCondition());
            return new WeDPRResponse(
                    Constant.WEDPR_SUCCESS,
                    Constant.WEDPR_SUCCESS_MSG,
                    new WedprPublishInvokeSearchResponse(
                            (new PageInfo<ServiceInvokeDO>(invokeDOList)).getTotal(),
                            invokeDOList));
        }
    }
}
