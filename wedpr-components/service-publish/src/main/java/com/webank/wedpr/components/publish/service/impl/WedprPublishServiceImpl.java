package com.webank.wedpr.components.publish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.dataset.dao.Dataset;
import com.webank.wedpr.components.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.publish.entity.WedprPublishService;
import com.webank.wedpr.components.publish.entity.request.PublishRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchReturn;
import com.webank.wedpr.components.publish.helper.PublishHelper;
import com.webank.wedpr.components.publish.mapper.WedprPublishServiceMapper;
import com.webank.wedpr.components.publish.sync.PublishSyncAction;
import com.webank.wedpr.components.publish.sync.api.PublishSyncerApi;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author zachma
 * @since 2024-08-28
 */
@Service
public class WedprPublishServiceImpl extends ServiceImpl<WedprPublishServiceMapper, WedprPublishService>
        implements com.webank.wedpr.components.publish.service.WedprPublishService {
    @Autowired private DatasetMapper datasetMapper;

    @Qualifier("publishSyncer")
    @Autowired
    PublishSyncerApi publishSyncer;

    @Override
    @Transactional(rollbackFor = WeDPRException.class)
    public WeDPRResponse createPublishService(String username, PublishRequest request)
            throws WeDPRException {
        try {
            checkCreatePublishRequest(request);
            String serviceId = PublishHelper.newServiceId();
            WedprPublishService wedprPublishService = new WedprPublishService();
            wedprPublishService.setOwner(username);
            wedprPublishService.setAgency(WeDPRCommonConfig.getAgency());
            wedprPublishService.setServiceDesc(request.getServiceDesc());
            wedprPublishService.setServiceType(request.getServiceType());
            wedprPublishService.setCreateTime(LocalDateTime.now());
            wedprPublishService.setServiceId(serviceId);
            wedprPublishService.setSearchConfig(request.getServiceConfig());
            if (this.save(wedprPublishService)) {
                publishSyncer.publishSync(wedprPublishService.serialize());
                return new WeDPRResponse(
                        Constant.WEDPR_SUCCESS,
                        Constant.WEDPR_SUCCESS_MSG,
                        new WedprPublishResponse(serviceId));
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, serviceId + "服务创建失败");
            }
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    @Override
    public void syncPublishService(PublishSyncAction action, WedprPublishService wedprPublishService) {
        String publishId = wedprPublishService.getPublishId();
        String publishUser = wedprPublishService.getPublishUser();
        if (action == PublishSyncAction.SYNC) {
            if (isPublishNotExist(publishId, publishUser)) {
                this.save(wedprPublishService);
            } else {
                LambdaQueryWrapper<WedprPublishService> wrapper =
                        new LambdaQueryWrapper<WedprPublishService>()
                                .eq(WedprPublishService::getPublishId, publishId);
                this.update(wedprPublishService, wrapper);
            }
        }

        if (action == PublishSyncAction.REVOKE) {
            if (!isPublishNotExist(publishId, publishUser)) {
                LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(WedprPublishService::getPublishId, publishId);
                this.remove(lambdaQueryWrapper);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = WeDPRException.class)
    public WeDPRResponse updatePublishService(String username, PublishRequest request) {
        if (isPublishNotExist(request.getPublishId(), username)) {
            return new WeDPRResponse(
                    Constant.WEDPR_FAILED, request.getPublishId() + "无法撤销，不属于用户" + username);
        }
        LambdaQueryWrapper<WedprPublishService> wrapper =
                new LambdaQueryWrapper<WedprPublishService>()
                        .eq(WedprPublishService::getPublishId, request.getPublishId());
        WedprPublishService wedprPublishService = new WedprPublishService();
        wedprPublishService.setPublishContent(request.getPublishContent());
        wedprPublishService.setSearchRule(request.getSearchRule());
        boolean updated = this.update(wedprPublishService, wrapper);
        if (updated) {
            publishSyncer.publishSync(wedprPublishService.serialize());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } else {
            return new WeDPRResponse(Constant.WEDPR_FAILED, request.getPublishId() + "服务更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = WeDPRException.class)
    public WeDPRResponse revokePublishService(String username, String publishId) {
        if (isPublishNotExist(publishId, username)) {
            return new WeDPRResponse(Constant.WEDPR_FAILED, publishId + "无法撤销，不属于用户" + username);
        }
        LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublishService::getPublishId, publishId)
                .eq(WedprPublishService::getPublishUser, username);
        boolean removed = this.remove(lambdaQueryWrapper);
        if (removed) {
            WedprPublishService wedprPublishService = new WedprPublishService();
            wedprPublishService.setPublishId(publishId);
            publishSyncer.revokeSync(wedprPublishService.serialize());
            return new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);
        } else {
            return new WeDPRResponse(Constant.WEDPR_FAILED, publishId + "服务删除失败");
        }
    }

    @SneakyThrows
    @Override
    public WeDPRResponse listPublishService(PublishSearchRequest request) {
        String username = request.getPublishUser();
        String publishName = request.getPublishName();
        String agency = request.getPublishAgency();
        String publishType = request.getPublishType();
        String publishDate = request.getPublishDate();
        LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            lambdaQueryWrapper.like(WedprPublishService::getPublishUser, username);
        }
        if (StringUtils.isNotBlank(publishName)) {
            lambdaQueryWrapper.like(WedprPublishService::getPublishName, publishName);
        }
        if (StringUtils.isNotBlank(agency)) {
            lambdaQueryWrapper.eq(WedprPublishService::getPublishAgency, agency);
        }
        if (StringUtils.isNotBlank(publishType)) {
            lambdaQueryWrapper.eq(WedprPublishService::getPublishType, publishType);
        }
        if (StringUtils.isNotBlank(publishDate)) {
            lambdaQueryWrapper.apply("DATE_FORMAT(publish_time, '%Y-%m-%d') = {0}", publishDate);
        }
        Page<WedprPublishService> wedprPublishPage =
                new Page<>(request.getPageNum(), request.getPageSize());
        Page<WedprPublishService> page = this.page(wedprPublishPage, lambdaQueryWrapper);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishSearchResponse(page.getTotal(), page.getRecords()));
    }

    @Override
    public WeDPRResponse searchPublishService(String publishId) {
        LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WedprPublishService::getPublishId, publishId);
        WedprPublishService wedprPublishService = this.getOne(lambdaQueryWrapper);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishSearchReturn(wedprPublishService));
    }

    public Boolean isPublishNotExist(String publishId, String userName) {
        LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublishService::getPublishId, publishId)
                .eq(WedprPublishService::getPublishAgency, WeDPRCommonConfig.getAgency())
                .eq(WedprPublishService::getPublishUser, userName);
        WedprPublishService queriedWedprPublishService = this.getOne(lambdaQueryWrapper);
        return Objects.isNull(queriedWedprPublishService);
    }

    private void checkCreatePublishRequest(PublishRequest request) throws WeDPRException {
        // 1.检验 publishType
        String[] types =
                Arrays.stream(PublishHelper.PublishType.values())
                        .map(PublishHelper.PublishType::getType)
                        .toArray(String[]::new);
        if (!Arrays.asList(types).contains(request.getPublishType())) {
            throw new WeDPRException("输入的类型必须为pir/xgb/lr");
        }

        // 2.检验dataset标头是否有id
        Dataset dataset = datasetMapper.getDatasetByDatasetId(request.getDatasetId(), false);
        String[] datasetFields =
                Arrays.stream(dataset.getDatasetFields().trim().split(","))
                        .map(String::trim)
                        .toArray(String[]::new);
        if (!Arrays.asList(datasetFields).contains(PublishHelper.PUBLISH_PIR_NEED_COLUMN)) {
            throw new WeDPRException("发布的数据集" + request.getDatasetId() + "必须含有id列");
        }
    }
}
