package com.webank.wedpr.components.dataset.sync.handler;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.dataset.permission.DatasetPermissionGenerator;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateActionHandler implements ActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateActionHandler.class);

    @Override
    public void handle(UserInfo userInfo, String content, ActionHandlerContext context)
            throws DatasetException {
        try {
            Dataset dataset =
                    ObjectMapperFactory.getObjectMapper().readValue(content, Dataset.class);

            String datasetId = dataset.getDatasetId();
            int visibility = dataset.getVisibility();
            String visibilityDetails = dataset.getVisibilityDetails();

            List<DatasetPermission> datasetPermissionList =
                    DatasetPermissionGenerator.generateDatasetVisibilityPermissions(
                            visibility, datasetId, userInfo, visibilityDetails, false);

            DatasetTransactionalWrapper datasetTransactionalWrapper =
                    context.getDatasetTransactionalWrapper();
            datasetTransactionalWrapper.transactionalAddDataset(
                    dataset.getDatasetId(), dataset, datasetPermissionList);

        } catch (DatasetException datasetException) {
            throw datasetException;
        } catch (Exception e) {
            logger.error(
                    "create action handle exception, user: {}, content: {}, e: ",
                    userInfo,
                    content,
                    e);
            throw new DatasetException("create action handle exception, e: " + e.getMessage());
        }
    }
}
