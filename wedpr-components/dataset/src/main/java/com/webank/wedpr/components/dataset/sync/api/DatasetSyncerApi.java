package com.webank.wedpr.components.dataset.sync.api;

import com.webank.wedpr.components.dataset.sync.DatasetSyncerAction;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import java.util.List;

public interface DatasetSyncerApi {

    void sync(UserInfo userInfo, DatasetSyncerAction action, String resourceContent);

    void syncCreateDataset(UserInfo userInfo, Dataset dataset);

    void syncDeleteDataset(UserInfo userInfo, List<String> datasetIdList);

    void syncUpdateDataset(UserInfo userInfo, List<Dataset> datasetList);
}
