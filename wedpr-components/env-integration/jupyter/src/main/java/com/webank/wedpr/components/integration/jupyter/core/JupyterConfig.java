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

package com.webank.wedpr.components.integration.jupyter.core;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.Common;
import org.apache.http.client.config.RequestConfig;

public class JupyterConfig {

    private static Integer MAX_JUPYTER_PER_HOST =
            WeDPRConfig.apply("wedpr.jupyter.max_count_per_host", 3);
    private static String JUPYTER_HOST_CONFIGURATION_KEY =
            WeDPRConfig.apply("wedpr.jupyter.host_configuration_key", "jupyter_entrypoints");
    private static String JUPYTER_ENTRYPOINT_SPLITTER = ";";
    private static String JUPYTER_MODULE = "jupyter-integration";
    private static String DEFAULT_JUPYTER_BINARY =
            WeDPRConfig.apply("wedpr.jupyter.binary", "jupyter-lab");
    private static String DEFAULT_JUPYTER_CONFIG_PATH =
            WeDPRConfig.apply(
                    "wedpr.juypter.config.path", "/home/share/.jupyter/jupyter_lab_config.py");
    private static String DEFAULT_JUPYTER_LISTEN_IP =
            WeDPRConfig.apply("wedpr.jupyter.listen.ip", "0.0.0.0");
    private static String DEFAULT_JUPYTER_PROJECT_DIR =
            WeDPRConfig.apply("wedpr.jupyter.project.dir", "project");
    private static Integer DEFAULT_JUPYTER_START_PORT =
            WeDPRConfig.apply("wedpr.jupyter.jupyter.default.startPort", 14001);

    ///// the parameter map key configuration for jupyter
    private static String PARAM_KEY_JUPYTER_CONFIG_PATH =
            WeDPRConfig.apply("wedpr.jupyter.param.key.jupyter_config_path", "jupyter_config_path");
    private static String PARAM_KEY_JUPYTER_PROJECT_PATH =
            WeDPRConfig.apply(
                    "wedpr.jupyter.param.key.jupyter_project_path", "jupyter_project_path");
    private static String PARAM_KEY_JUPYTER_BINARY =
            WeDPRConfig.apply("wedpr.jupyter.param.key.jupyter_binary", "jupyter_binary");
    private static String PARAM_KEY_JUPYTER_AUTH_SECRET_FILE_PATH =
            WeDPRConfig.apply(
                    "wedpr.jupyter.param.key.auth_secret_file_path", "auth_secret_file_path");
    private static String JUPYTER_AUTH_SECRET_FILE_PATH =
            WeDPRConfig.apply(
                    "wedpr.jupyter.auth_secret_file_path", "/home/share/jupyter_secret_file");
    ///// the parameter map key configuration for jupyter

    //// the key to store the jupyter code template
    private static String CODE_TEMPLATE_KEY_START_JUPYTER =
            WeDPRConfig.apply("wedpr.code.template.key.start_jupyter", "wedpr_start_jupyter");
    private static String CODE_TEMPLATE_KEY_GET_JUPYTER_PID =
            WeDPRConfig.apply(
                    "wedpr.code.template.key.wedpr_get_jupyter_pid", "wedpr_get_jupyter_pid");
    //// the key to store the jupyter code template

    /// the jupyter worker client config ///
    private static final Integer CONNECTION_REQUEST_TIME_OUT =
            WeDPRConfig.apply("wedpr.jupyter.connect.request.timeout.ms", 10000);
    private static final Integer CONNECTION_TIME_OUT =
            WeDPRConfig.apply("wedpr.jupyter.connect.timeout.ms", 5000);
    private static final Integer REQUEST_TIMEOUT =
            WeDPRConfig.apply("wedpr.jupyter.request.timeout.ms", 60000);
    private static final Integer MAX_TOTAL_CONNECTION =
            WeDPRConfig.apply("wedpr.jupyter.max.total.connection", 5);
    /// the jupyter worker client config ///

    private static String DEFAULT_HOME_DIR = "/home";

    public static String getJupyterHostConfigurationKey() {
        return JUPYTER_HOST_CONFIGURATION_KEY;
    }

    public static Integer getMaxJupyterPerHost() {
        return MAX_JUPYTER_PER_HOST;
    }

    public static String getJupyterEntrypointSplitter() {
        return JUPYTER_ENTRYPOINT_SPLITTER;
    }

    public static String getJupyterModule() {
        return JUPYTER_MODULE;
    }

    public static String getDefaultJupyterBinary() {
        return DEFAULT_JUPYTER_BINARY;
    }

    public static String getDefaultJupyterConfigPath() {
        return DEFAULT_JUPYTER_CONFIG_PATH;
    }

    public static String getDefaultJupyterListenIp() {
        return DEFAULT_JUPYTER_LISTEN_IP;
    }

    public static String getDefaultJupyterProjectDir() {
        return DEFAULT_JUPYTER_PROJECT_DIR;
    }

    public static String getUserJupyterProjectPath(String userName) {
        return Common.joinPath(
                Common.joinPath(DEFAULT_HOME_DIR, userName), getDefaultJupyterProjectDir());
    }

    public static Integer getDefaultJupyterStartPort() {
        return DEFAULT_JUPYTER_START_PORT;
    }

    public static String getParamKeyJupyterConfigPath() {
        return PARAM_KEY_JUPYTER_CONFIG_PATH;
    }

    public static String getParamKeyJupyterProjectPath() {
        return PARAM_KEY_JUPYTER_PROJECT_PATH;
    }

    public static String getParamKeyJupyterBinary() {
        return PARAM_KEY_JUPYTER_BINARY;
    }

    public static String getCodeTemplateKeyStartJupyter() {
        return CODE_TEMPLATE_KEY_START_JUPYTER;
    }

    public static String getCodeTemplateKeyGetJupyterPid() {
        return CODE_TEMPLATE_KEY_GET_JUPYTER_PID;
    }

    public static RequestConfig buildConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT)
                .setSocketTimeout(REQUEST_TIMEOUT)
                .build();
    }

    public static Integer getMaxTotalConnection() {
        return MAX_TOTAL_CONNECTION;
    }

    public static String getParamKeyJupyterAuthSecretFilePath() {
        return PARAM_KEY_JUPYTER_AUTH_SECRET_FILE_PATH;
    }

    public static String getJupyterAuthSecretFilePath() {
        return JUPYTER_AUTH_SECRET_FILE_PATH;
    }
}
