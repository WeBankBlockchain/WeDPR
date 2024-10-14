package com.webank.wedpr.components.dataset.sync.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.components.db.mapper.dataset.mapper.wapper.DatasetTransactionalWrapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveActionHandler implements ActionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoveActionHandler.class);

    private DatasetTransactionalWrapper datasetTransactionalWrapper;

    @Override
    public void handle(UserInfo userInfo, String content, ActionHandlerContext context)
            throws DatasetException {

        try {
            List<String> datasetIdList =
                    ObjectMapperFactory.getObjectMapper()
                            .readValue(content, new TypeReference<List<String>>() {});
            context.getDatasetTransactionalWrapper().transactionalDeleteDatasetList(datasetIdList);
        } catch (Exception e) {
            logger.error(
                    "delete action handle exception, user: {}, content: {}, e: ",
                    userInfo,
                    content,
                    e);
            throw new DatasetException("delete action handle exception, e: " + e.getMessage());
        }
    }
}
