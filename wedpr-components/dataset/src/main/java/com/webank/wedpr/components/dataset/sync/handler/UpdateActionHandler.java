package com.webank.wedpr.components.dataset.sync.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.components.dataset.permission.DatasetPermissionGenerator;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateActionHandler implements ActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateActionHandler.class);

    @Override
    public void handle(UserInfo userInfo, String content, ActionHandlerContext context)
            throws DatasetException {

        try {
            List<Dataset> datasetList =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(content, new TypeReference<List<Dataset>>() {});

            List<List<DatasetPermission>> datasetPermissionListList = new ArrayList<>();
            for (Dataset dataset : datasetList) {
                int visibility = dataset.getVisibility();
                String visibilityDetails = dataset.getVisibilityDetails();

                List<DatasetPermission> datasetPermissionList =
                        DatasetPermissionGenerator.generateDatasetVisibilityPermissions(
                                visibility,
                                dataset.getDatasetId(),
                                userInfo,
                                visibilityDetails,
                                false);
                datasetPermissionListList.add(datasetPermissionList);
            }

            DatasetTransactionalWrapper datasetTransactionalWrapper =
                    context.getDatasetTransactionalWrapper();
            datasetTransactionalWrapper.transactionalUpdateDatasetList(
                    datasetList, datasetPermissionListList);

        } catch (Exception e) {
            logger.error(
                    "update action handle exception, user: {}, content: {}, e: ",
                    userInfo,
                    content,
                    e);
            throw new DatasetException("update action handle exception, e: " + e.getMessage());
        }
    }
}
