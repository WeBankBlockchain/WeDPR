package com.webank.wedpr.components.publish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.publish.entity.WedprPublishRecord;
import com.webank.wedpr.components.publish.entity.request.NodeInvokeRequest;
import com.webank.wedpr.components.publish.entity.request.PublishRecordRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishRecordSearchResponse;
import com.webank.wedpr.components.publish.mapper.WedprPublishRecordMapper;
import com.webank.wedpr.components.publish.service.WedprPublishRecordService;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author zachma
 * @since 2024-08-28
 */
@Service
public class WedprPublishRecordServiceImpl
        extends ServiceImpl<WedprPublishRecordMapper, WedprPublishRecord>
        implements WedprPublishRecordService {

    @Override
    public WeDPRResponse seachPublishRecordService(
            String publishId, PublishRecordRequest publishRecordRequest) {
        LambdaQueryWrapper<WedprPublishRecord> wrapper =
                new LambdaQueryWrapper<WedprPublishRecord>()
                        .eq(WedprPublishRecord::getPublishId, publishId);
        String invokeAgency = publishRecordRequest.getInvokeAgency();
        String expireDate = publishRecordRequest.getExpireDate();
        String invokeStatus = publishRecordRequest.getInvokeStatus();
        String invokeDate = publishRecordRequest.getInvokeDate();

        if (StringUtils.isNotBlank(invokeAgency)) {
            wrapper.eq(WedprPublishRecord::getInvokeAgency, invokeAgency);
        }
        if (StringUtils.isNotBlank(invokeStatus)) {
            wrapper.eq(WedprPublishRecord::getInvokeStatus, invokeStatus);
        }

        if (StringUtils.isNotBlank(invokeDate)) {
            wrapper.apply("DATE_FORMAT(invoke_time, '%Y-%m-%d') = {0}", invokeDate);
        }

        if (StringUtils.isNotBlank(expireDate)) {
            wrapper.apply("DATE_FORMAT(expire_time, '%Y-%m-%d') = {0}", expireDate);
        }

        Page<WedprPublishRecord> wedprPublishRecordPage =
                new Page<>(publishRecordRequest.getPageNum(), publishRecordRequest.getPageSize());
        Page<WedprPublishRecord> page = this.page(wedprPublishRecordPage, wrapper);

        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishRecordSearchResponse(page.getTotal(), page.getRecords()));
    }

    @Override
    public WeDPRResponse invokePublishRecordService(NodeInvokeRequest nodeInvokeRequest) {
        // TODO: 通过publishId 查找 wedpr_publish_token 申请时间和过期时间
        WedprPublishRecord wedprPublishRecord = new WedprPublishRecord();
        wedprPublishRecord.setInvokeAgency(nodeInvokeRequest.getInvokeAgency());
        wedprPublishRecord.setInvokeUser(nodeInvokeRequest.getInvokeUser());
        wedprPublishRecord.setInvokeId(WeDPRUuidGenerator.generateID());
        wedprPublishRecord.setPublishId(nodeInvokeRequest.getPublishId());
        wedprPublishRecord.setInvokeStatus(nodeInvokeRequest.getInvokeStatus());
        wedprPublishRecord.setInvokeTime(nodeInvokeRequest.getInvokeTime());
        boolean save = this.save(wedprPublishRecord);
        if (save) {
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } else {
            return new WeDPRResponse(Constant.WEDPR_FAILED, "invokePublishRecord failed");
        }
    }
}
