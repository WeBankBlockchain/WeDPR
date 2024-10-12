/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.scheduler.executor.impl.helper;

import com.webank.wedpr.components.storage.api.FileStorageInterface;
import com.webank.wedpr.core.config.WeDPRCommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorHelper {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorHelper.class);

    public static String uploadJobResult(
            FileStorageInterface storage,
            String localFilePath,
            String userGroup,
            String owner,
            String jobType,
            String jobID,
            String remoteFileName,
            boolean isAbsolute) {
        long startT = System.currentTimeMillis();
        String remotePath =
                WeDPRCommonConfig.getUserJobCachePath(owner, jobType, jobID, remoteFileName);
        FileStorageInterface.FilePermissionInfo permissionInfo =
                new FileStorageInterface.FilePermissionInfo(owner, userGroup);
        storage.upload(permissionInfo, Boolean.TRUE, localFilePath, remotePath, isAbsolute);
        logger.info(
                "uploadJobResult, jobId: {}, localFilePath: {}, remotePath: {}, timecost: {}",
                jobID,
                localFilePath,
                remotePath,
                (System.currentTimeMillis() - startT));
        return storage.generateAbsoluteDir(remotePath);
    }
}
