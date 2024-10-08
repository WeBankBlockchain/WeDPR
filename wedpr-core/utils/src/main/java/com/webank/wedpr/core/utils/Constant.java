/*
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package com.webank.wedpr.core.utils;

public class Constant {
    public static final String STR_SEPARATOR = ",";
    public static final String DEFAULT_IV = "1234567890";

    public static final String CERT_SCRIPT_EXECUTE_OK = "successful";
    public static final String ZIP_FILE_SUFFIX = ".zip";
    public static Integer WEDPR_SUCCESS = 0;
    public static Integer HTTP_SUCCESS = 200;
    public static String WEDPR_SUCCESS_MSG = "success";

    public static Integer WEDPR_FAILED = -1;

    public static String CHAIN_CONFIG_FILE = "config.toml";

    public static final String WEDPR_API_PREFIX = "/api/wedpr/v3";

    public static final String TOKEN_FIELD = "Authorization";
    // the userToken information defined in the request header
    public static final String REQUEST_USER_TOKEN_FIELD = "UserToken";
    // the auth type field defined in the request header
    public static final String REQUEST_AUTH_TYPE_FIELD = "AuthType";

    public static final String USER_TOKEN_CLAIM = "user";

    public static final String SYS_USER = "sys";
    public static final String DEFAULT_LINE_SPLITTER = "\n";
    public static final String DEFAULT_LABEL_FIELD = "y";
    public static final String DEFAULT_ID_FIELD = "id";

    public static final String DEFAULT_TOKEN_TYPE = "jwt";
    public static final String DEFAULT_OPEN_SYMBOL = "*";

    /** @Fields DEFAULT_PAGE_OFFSET:分页起始页 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** @Fields DEFAULT_PAGE_SIZE:分页的每页记录数 */
    public static final int DEFAULT_PAGE_SIZE = 1;

    public static final int DEFAULT_REPORT_PAGE_SIZE = 1000;

    public static final String SITE_END_LOGIN_URL = WEDPR_API_PREFIX + "/login";
    public static final String ADMIN_END_LOGIN_URL = WEDPR_API_PREFIX + "/admin/login";

    public static final String REGISTER_URL = WEDPR_API_PREFIX + "/register";
    public static final String USER_PUBLICKEY_URL = WEDPR_API_PREFIX + "/pub";
    public static final String IMAGE_CODE_URL = WEDPR_API_PREFIX + "/image-code";
    public static final String ENCRYPT_PREFIX = "{bcrypt}";
    public static final String DEFAULT_SPLITTER = "-";
    public static final String DEFAULT_NULL_STR = "null";
    public static final Integer DuplicatedTaskCode = -201;

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_INIT_GROUP_ID = "1000000000000000";

    public static final String PID_FIELD = "pid";
    public static final String DATE_VAR = "datetime";
    public static final String LOG_POSTFIX = ".log";

    public static final String PUBLISH_ID_PREFIX = "s-";
    public static final String DEFAULT_JOB_GROUP = "wedpr";
}
