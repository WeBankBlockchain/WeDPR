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

package com.webank.wedpr.core.config;

import com.webank.wedpr.core.utils.Common;

public class WeDPRCommonConfig {
    private static final Integer DEFAULT_READ_TRUNK_SIZE = 1024 * 1024;
    private static final Integer DEFAULT_WRITE_TRUNK_SIZE = 1024 * 1024;
    // the agency id
    private static final String AGENCY = WeDPRConfig.apply("wedpr.agency", null, Boolean.TRUE);
    private static final String ADMIN_AGENCY =
            WeDPRConfig.apply("wedpr.admin_agency", "ADMIN", Boolean.TRUE);
    private static final String FIELD_SPLITTER = WeDPRConfig.apply("wedpr.field.splitter", ",");

    private static final Integer READ_CHUNK_SIZE =
            WeDPRConfig.apply("wedpr.file.read.lines", DEFAULT_READ_TRUNK_SIZE);
    private static final Integer WRITE_CHUNK_SIZE =
            WeDPRConfig.apply("wedpr.file.write.lines", DEFAULT_WRITE_TRUNK_SIZE);
    private static final String CACHE_DIR = WeDPRConfig.apply("wedpr.cache.dir", "/home/cache");

    private static final Integer AUTH_CACHE_SIZE =
            WeDPRConfig.apply("wedpr.auth.cache.size", 10000);
    private static final Integer AUTH_CACHE_EXPIRE_TIME =
            WeDPRConfig.apply("wedpr.auth.cache.expire.minutes", 30);
    private static final Integer REPORT_TIMEOUT =
            WeDPRConfig.apply("wedpr.report.timeout.seconds", 30);

    public static String getAgency() {
        return AGENCY;
    }

    public static String getAdminAgency() {
        return ADMIN_AGENCY;
    }

    public static String getFieldSplitter() {
        return FIELD_SPLITTER;
    }

    public static Integer getReadChunkSize() {
        return READ_CHUNK_SIZE;
    }

    public static Integer getWriteChunkSize() {
        return WRITE_CHUNK_SIZE;
    }

    public static String getCacheDir() {
        return CACHE_DIR;
    }

    public static String getUserCacheDir(String userName) {
        return Common.joinPath(CACHE_DIR, userName);
    }

    public static String getUserJobCacheDir(String userName, String jobID) {
        return Common.joinPath(getUserCacheDir(userName), jobID);
    }

    public static String getUserJobCachePath(String user, String jobID, String file) {
        return Common.joinPath(getUserJobCacheDir(user, jobID), file);
    }

    public static String getUserDatasetPath(String user, String datasetId) {
        return Common.joinPath(user, datasetId);
    }

    public static Integer getAuthCacheSize() {
        return AUTH_CACHE_SIZE;
    }

    public static Integer getAuthCacheExpireTime() {
        return AUTH_CACHE_EXPIRE_TIME;
    }

    public static Integer getReportTimeout() {
        return REPORT_TIMEOUT;
    }
}
