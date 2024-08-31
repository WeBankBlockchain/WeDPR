package com.webank.wedpr.components.publish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.dataset.dao.Dataset;
import com.webank.wedpr.components.dataset.mapper.DatasetMapper;
<<<<<<< HEAD
import com.webank.wedpr.components.publish.entity.WedprPublishService;
=======
import com.webank.wedpr.components.publish.entity.WedprPublish;
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
import com.webank.wedpr.components.publish.entity.request.PublishRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchReturn;
import com.webank.wedpr.components.publish.helper.PublishHelper;
<<<<<<< HEAD
import com.webank.wedpr.components.publish.mapper.WedprPublishServiceMapper;
=======
import com.webank.wedpr.components.publish.mapper.WedprPublishMapper;
import com.webank.wedpr.components.publish.service.WedprPublishService;
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
<<<<<<< HEAD
public class WedprPublishServiceImpl extends ServiceImpl<WedprPublishServiceMapper, WedprPublishService>
        implements com.webank.wedpr.components.publish.service.WedprPublishService {
=======
public class WedprPublishServiceImpl extends ServiceImpl<WedprPublishMapper, WedprPublish>
        implements WedprPublishService {
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
<<<<<<< HEAD
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
=======
            String publishId = PublishHelper.newPublishId();
            WedprPublish wedprPublish = new WedprPublish();
            wedprPublish.setPublishUser(username);
            wedprPublish.setPublishAgency(WeDPRCommonConfig.getAgency());
            wedprPublish.setPublishContent(request.getPublishContent());
            wedprPublish.setPublishName(request.getPublishName());
            wedprPublish.setPublishTime(LocalDateTime.now());
            wedprPublish.setPublishId(publishId);
            wedprPublish.setDatasetId(request.getDatasetId());
            wedprPublish.setSearchRule(request.getSearchRule());
            wedprPublish.setPublishType(request.getPublishType());
            if (this.save(wedprPublish)) {
                publishSyncer.publishSync(wedprPublish.serialize());
                return new WeDPRResponse(
                        Constant.WEDPR_SUCCESS,
                        Constant.WEDPR_SUCCESS_MSG,
                        new WedprPublishResponse(publishId));
            } else {
                return new WeDPRResponse(Constant.WEDPR_FAILED, publishId + "服务创建失败");
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
            }
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    @Override
<<<<<<< HEAD
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
=======
    public void syncPublishService(PublishSyncAction action, WedprPublish wedprPublish) {
        String publishId = wedprPublish.getPublishId();
        String publishUser = wedprPublish.getPublishUser();
        if (action == PublishSyncAction.SYNC) {
            if (isPublishNotExist(publishId, publishUser)) {
                this.save(wedprPublish);
            } else {
                LambdaQueryWrapper<WedprPublish> wrapper =
                        new LambdaQueryWrapper<WedprPublish>()
                                .eq(WedprPublish::getPublishId, publishId);
                this.update(wedprPublish, wrapper);
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
            }
        }

        if (action == PublishSyncAction.REVOKE) {
            if (!isPublishNotExist(publishId, publishUser)) {
<<<<<<< HEAD
                LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(WedprPublishService::getPublishId, publishId);
=======
                LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(WedprPublish::getPublishId, publishId);
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
<<<<<<< HEAD
        LambdaQueryWrapper<WedprPublishService> wrapper =
                new LambdaQueryWrapper<WedprPublishService>()
                        .eq(WedprPublishService::getPublishId, request.getPublishId());
        WedprPublishService wedprPublishService = new WedprPublishService();
        wedprPublishService.setPublishContent(request.getPublishContent());
        wedprPublishService.setSearchRule(request.getSearchRule());
        boolean updated = this.update(wedprPublishService, wrapper);
        if (updated) {
            publishSyncer.publishSync(wedprPublishService.serialize());
=======
        LambdaQueryWrapper<WedprPublish> wrapper =
                new LambdaQueryWrapper<WedprPublish>()
                        .eq(WedprPublish::getPublishId, request.getPublishId());
        WedprPublish wedprPublish = new WedprPublish();
        wedprPublish.setPublishContent(request.getPublishContent());
        wedprPublish.setSearchRule(request.getSearchRule());
        boolean updated = this.update(wedprPublish, wrapper);
        if (updated) {
            publishSyncer.publishSync(wedprPublish.serialize());
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
<<<<<<< HEAD
        LambdaQueryWrapper<WedprPublishService> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublishService::getPublishId, publishId)
                .eq(WedprPublishService::getPublishUser, username);
        boolean removed = this.remove(lambdaQueryWrapper);
        if (removed) {
            WedprPublishService wedprPublishService = new WedprPublishService();
            wedprPublishService.setPublishId(publishId);
            publishSyncer.revokeSync(wedprPublishService.serialize());
=======
        LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublish::getPublishId, publishId)
                .eq(WedprPublish::getPublishUser, username);
        boolean removed = this.remove(lambdaQueryWrapper);
        if (removed) {
            WedprPublish wedprPublish = new WedprPublish();
            wedprPublish.setPublishId(publishId);
            publishSyncer.revokeSync(wedprPublish.serialize());
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
<<<<<<< HEAD
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
=======
        LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            lambdaQueryWrapper.like(WedprPublish::getPublishUser, username);
        }
        if (StringUtils.isNotBlank(publishName)) {
            lambdaQueryWrapper.like(WedprPublish::getPublishName, publishName);
        }
        if (StringUtils.isNotBlank(agency)) {
            lambdaQueryWrapper.eq(WedprPublish::getPublishAgency, agency);
        }
        if (StringUtils.isNotBlank(publishType)) {
            lambdaQueryWrapper.eq(WedprPublish::getPublishType, publishType);
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
        }
        if (StringUtils.isNotBlank(publishDate)) {
            lambdaQueryWrapper.apply("DATE_FORMAT(publish_time, '%Y-%m-%d') = {0}", publishDate);
        }
<<<<<<< HEAD
        Page<WedprPublishService> wedprPublishPage =
                new Page<>(request.getPageNum(), request.getPageSize());
        Page<WedprPublishService> page = this.page(wedprPublishPage, lambdaQueryWrapper);
=======
        Page<WedprPublish> wedprPublishPage =
                new Page<>(request.getPageNum(), request.getPageSize());
        Page<WedprPublish> page = this.page(wedprPublishPage, lambdaQueryWrapper);
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishSearchResponse(page.getTotal(), page.getRecords()));
    }

    @Override
    public WeDPRResponse searchPublishService(String publishId) {
<<<<<<< HEAD
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
=======
        LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WedprPublish::getPublishId, publishId);
        WedprPublish wedprPublish = this.getOne(lambdaQueryWrapper);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishSearchReturn(wedprPublish));
    }

    public Boolean isPublishNotExist(String publishId, String userName) {
        LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublish::getPublishId, publishId)
                .eq(WedprPublish::getPublishAgency, WeDPRCommonConfig.getAgency())
                .eq(WedprPublish::getPublishUser, userName);
        WedprPublish queriedWedprPublish = this.getOne(lambdaQueryWrapper);
        return Objects.isNull(queriedWedprPublish);
>>>>>>> c0ce0d5bc82d8ee00533f54c5ed4ad108ea0cd96
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
