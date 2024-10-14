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

package com.webank.wedpr.common.config;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.WeDPRException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

// Note: Since the WeDPRCommonConfig module is a reusable, it shouldn't require the config to exist
// when initializing static configuration variables,
// which will cause some services that do not need to set these configuration to exception.
public class WeDPRCommonConfig {
    private static final Integer DEFAULT_READ_TRUNK_SIZE = 1024 * 1024;
    private static final Integer DEFAULT_WRITE_TRUNK_SIZE = 1024 * 1024;
    // the agency id
    private static final String AGENCY = WeDPRConfig.apply("wedpr.agency", null);
    private static final String ADMIN_AGENCY = WeDPRConfig.apply("wedpr.admin_agency", null);
    private static final String FIELD_SPLITTER = WeDPRConfig.apply("wedpr.field.splitter", ",");

    private static final Integer READ_CHUNK_SIZE =
            WeDPRConfig.apply("wedpr.file.read.lines", DEFAULT_READ_TRUNK_SIZE);
    private static final Integer WRITE_CHUNK_SIZE =
            WeDPRConfig.apply("wedpr.file.write.lines", DEFAULT_WRITE_TRUNK_SIZE);
    private static final String SHARE_DIR = WeDPRConfig.apply("wedpr.share.dir", "share");

    private static final Integer AUTH_CACHE_SIZE =
            WeDPRConfig.apply("wedpr.auth.cache.size", 10000);
    private static final Integer AUTH_CACHE_EXPIRE_TIME =
            WeDPRConfig.apply("wedpr.auth.cache.expire.minutes", 30);
    private static final Integer REPORT_TIMEOUT =
            WeDPRConfig.apply("wedpr.report.timeout.seconds", 30);
    private static final Integer REPORT_TIMEOUT_MS = REPORT_TIMEOUT * 1000;

    //// the param key configuration
    private static String PARAM_KEY_USER_NAME =
            WeDPRConfig.apply("wedpr.param.key.user", "user_name");
    private static String PARAM_KEY_LISTEN_IP =
            WeDPRConfig.apply("wedpr.param.key.listen_ip", "listen_ip");
    private static String PARAM_KEY_LISTEN_PORT =
            WeDPRConfig.apply("wedpr.param.key.listen_port", "listen_port");
    //// the param key configuration

    //// the key to store the jupyter code template
    private static String CODE_TEMPLATE_KEY_CREATE_USER =
            WeDPRConfig.apply("wedpr.code.template.key.create_user", "wedpr_create_user");
    private static String CODE_TEMPLATE_KEY_DELETE_USER =
            WeDPRConfig.apply("wedpr.code.template.key.delete", "wedpr_delete_user");
    //// the key to store the jupyter code template

    private static String WEDPR_WORKER_API_PATH =
            WeDPRConfig.apply("wedpr.worker.api.path", "/api/wedpr/v3/worker");
    private static String WEDPR_WORKER_SUBMIT_TASK_METHOD =
            WeDPRConfig.apply("wedpr.worker.api.method.submit", "submit");

    private static String SHELL_CODE_CONNECTOR = " && ";

    @SneakyThrows
    public static String getAgency() {
        if (StringUtils.isBlank(AGENCY)) {
            throw new WeDPRException("Invalid emtpy agency!");
        }
        return AGENCY;
    }

    @SneakyThrows
    public static String getAdminAgency() {
        if (StringUtils.isBlank(ADMIN_AGENCY)) {
            throw new WeDPRException("Invalid emtpy agency!");
        }
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

    public static String getUserShareDir(String userName) {
        return Common.joinPath(userName, SHARE_DIR);
    }

    public static String getUserJobCacheDir(String userName, String jobType, String jobID) {
        return Common.joinPath(getUserShareDir(userName), jobType.toLowerCase() + "-" + jobID);
    }

    public static String getUserJobCachePath(
            String user, String jobType, String jobID, String file) {
        return Common.joinPath(getUserJobCacheDir(user, jobType, jobID), file);
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

    public static Integer getReportTimeoutMs() {
        return REPORT_TIMEOUT_MS;
    }

    public static String getParamKeyUserName() {
        return PARAM_KEY_USER_NAME;
    }

    public static String getParamKeyListenIp() {
        return PARAM_KEY_LISTEN_IP;
    }

    public static String getParamKeyListenPort() {
        return PARAM_KEY_LISTEN_PORT;
    }

    public static String getCodeTemplateKeyCreateUser() {
        return CODE_TEMPLATE_KEY_CREATE_USER;
    }

    public static String getCodeTemplateKeyDeleteUser() {
        return CODE_TEMPLATE_KEY_DELETE_USER;
    }

    public static String getShellCodeConnector() {
        return SHELL_CODE_CONNECTOR;
    }

    public static String getWedprWorkerApiPath() {
        return WEDPR_WORKER_API_PATH;
    }

    public static String getWedprWorkerSubmitTaskMethod() {
        return WEDPR_WORKER_SUBMIT_TASK_METHOD;
    }
}
