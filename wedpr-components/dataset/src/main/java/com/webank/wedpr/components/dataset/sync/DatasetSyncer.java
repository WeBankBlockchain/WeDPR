package com.webank.wedpr.components.dataset.sync;

import com.webank.wedpr.components.dataset.sync.api.DatasetSyncerApi;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.components.sync.ResourceSyncer;
import com.webank.wedpr.components.sync.core.ResourceActionRecord;
import com.webank.wedpr.components.sync.core.ResourceActionRecorderBuilder;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class DatasetSyncer implements DatasetSyncerApi {

    private static final Logger logger = LoggerFactory.getLogger(DatasetSyncer.class);

    private String agency;
    private String resourceType;
    private ResourceSyncer resourceSyncer;
    private ResourceActionRecorderBuilder resourceBuilder;
    private DatasetTransactionalWrapper transactionalWrapper;

    @Override
    public void sync(UserInfo userInfo, DatasetSyncerAction action, String resourceContent) {

        String syncId = WeDPRUuidGenerator.generateID();

        ResourceActionRecord resourceActionRecord =
                this.resourceBuilder.build(syncId, action.getAction(), resourceContent);
        this.resourceSyncer.sync(userInfo.getUser(), resourceActionRecord);

        logger.info(
                "sync dataset, syncId: {}, action: {}, content: {}",
                syncId,
                action,
                resourceActionRecord);
    }

    @Override
    public void syncCreateDataset(UserInfo userInfo, Dataset dataset) {
        try {
            dataset.resetMeta();
            String content = ObjectMapperFactory.getObjectMapper().writeValueAsString(dataset);
            sync(userInfo, DatasetSyncerAction.CREATE, content);
        } catch (Exception e) {
            logger.error("sync create dataset failed, e: ", e);
        }
    }

    @Override
    public void syncDeleteDataset(UserInfo userInfo, List<String> datasetIdList) {
        try {
            String content =
                    ObjectMapperFactory.getObjectMapper().writeValueAsString(datasetIdList);
            sync(userInfo, DatasetSyncerAction.REMOVE, content);
        } catch (Exception e) {
            logger.error("sync create dataset failed, e: ", e);
        }
    }

    @Override
    public void syncUpdateDataset(UserInfo userInfo, List<Dataset> datasetList) {
        try {
            String content = ObjectMapperFactory.getObjectMapper().writeValueAsString(datasetList);
            sync(userInfo, DatasetSyncerAction.UPDATE, content);
        } catch (Exception e) {
            logger.error("sync create dataset failed, e: ", e);
        }
    }
}
