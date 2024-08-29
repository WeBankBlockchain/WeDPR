package com.webank.wedpr.components.publish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.dataset.dao.Dataset;
import com.webank.wedpr.components.dataset.mapper.DatasetMapper;
import com.webank.wedpr.components.publish.entity.WedprPublish;
import com.webank.wedpr.components.publish.entity.request.PublishRequest;
import com.webank.wedpr.components.publish.entity.request.PublishSearchRequest;
import com.webank.wedpr.components.publish.entity.response.WedprPublishResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchResponse;
import com.webank.wedpr.components.publish.entity.response.WedprPublishSearchReturn;
import com.webank.wedpr.components.publish.helper.PublishHelper;
import com.webank.wedpr.components.publish.mapper.WedprPublishMapper;
import com.webank.wedpr.components.publish.service.WedprPublishService;
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
public class WedprPublishServiceImpl extends ServiceImpl<WedprPublishMapper, WedprPublish>
        implements WedprPublishService {
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
            }
        } catch (Exception e) {
            throw new WeDPRException(e.getMessage());
        }
    }

    @Override
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
            }
        }

        if (action == PublishSyncAction.REVOKE) {
            if (!isPublishNotExist(publishId, publishUser)) {
                LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(WedprPublish::getPublishId, publishId);
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
        LambdaQueryWrapper<WedprPublish> wrapper =
                new LambdaQueryWrapper<WedprPublish>()
                        .eq(WedprPublish::getPublishId, request.getPublishId());
        WedprPublish wedprPublish = new WedprPublish();
        wedprPublish.setPublishContent(request.getPublishContent());
        wedprPublish.setSearchRule(request.getSearchRule());
        boolean updated = this.update(wedprPublish, wrapper);
        if (updated) {
            publishSyncer.publishSync(wedprPublish.serialize());
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
        LambdaQueryWrapper<WedprPublish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(WedprPublish::getPublishId, publishId)
                .eq(WedprPublish::getPublishUser, username);
        boolean removed = this.remove(lambdaQueryWrapper);
        if (removed) {
            WedprPublish wedprPublish = new WedprPublish();
            wedprPublish.setPublishId(publishId);
            publishSyncer.revokeSync(wedprPublish.serialize());
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
        }
        if (StringUtils.isNotBlank(publishDate)) {
            lambdaQueryWrapper.apply("DATE_FORMAT(publish_time, '%Y-%m-%d') = {0}", publishDate);
        }
        Page<WedprPublish> wedprPublishPage =
                new Page<>(request.getPageNum(), request.getPageSize());
        Page<WedprPublish> page = this.page(wedprPublishPage, lambdaQueryWrapper);
        return new WeDPRResponse(
                Constant.WEDPR_SUCCESS,
                Constant.WEDPR_SUCCESS_MSG,
                new WedprPublishSearchResponse(page.getTotal(), page.getRecords()));
    }

    @Override
    public WeDPRResponse searchPublishService(String publishId) {
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
