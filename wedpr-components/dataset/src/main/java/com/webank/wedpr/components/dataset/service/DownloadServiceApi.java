package com.webank.wedpr.components.dataset.service;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import java.io.OutputStream;

public interface DownloadServiceApi {

    int getFileShardsInfo(UserInfo userInfo, String filePath) throws WeDPRException;

    void downloadFileShardData(
            UserInfo userInfo,
            String filePath,
            int shardCount,
            int shardIndex,
            OutputStream outputStream)
            throws WeDPRException;
}
