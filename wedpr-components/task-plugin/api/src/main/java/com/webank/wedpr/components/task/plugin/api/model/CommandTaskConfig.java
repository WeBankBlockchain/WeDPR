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
package com.webank.wedpr.components.task.plugin.api.model;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.components.task.plugin.api.shell.ShellType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class CommandTaskConfig {
    private static final String DEFAULT_EXECUTE_DIR =
            WeDPRConfig.apply("task.exec.dir", "/data/app");
    private static final String DEFAULT_LOG_BASE_PATH =
            WeDPRConfig.apply("task.log.base.path", ".");
    private static final List<String> SYSTEM_ENV_FILES =
            Arrays.stream(
                            Optional.ofNullable(WeDPRConfig.apply("task.system.env", ""))
                                    .map(s -> s.split(","))
                                    .orElse(new String[0]))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
    private static final String SHELL_OUTPUT_FILE_PREFIX =
            WeDPRConfig.apply("task.exec.output.prefix.file", "output");
    private static final Integer KILL_DEFAULT_TIMEOUT_SECONDS =
            WeDPRConfig.apply("task.kill.timeout.seconds", 5);

    private static final Boolean ENABLE_SUDO =
            WeDPRConfig.apply("task.exec.enable.sudo", Boolean.FALSE);

    private static final String SHELL_TYPE =
            WeDPRConfig.apply("task.shell.type", ShellType.BASH.getType());

    public static String getDefaultExecuteDir() {
        return DEFAULT_EXECUTE_DIR;
    }

    public static String getLogPath(String appName) {
        return Common.joinPath(DEFAULT_LOG_BASE_PATH, appName + Constant.LOG_POSTFIX);
    }
    // the system env files
    public static List<String> getSystemEnvFiles() {
        return SYSTEM_ENV_FILES;
    }

    public static Boolean getEnableSudo() {
        return ENABLE_SUDO;
    }

    public static String getOutputFile(String appName) {
        return SHELL_OUTPUT_FILE_PREFIX + "_" + appName + Constant.LOG_POSTFIX;
    }

    public static Integer getKillDefaultTimeoutSeconds() {
        return KILL_DEFAULT_TIMEOUT_SECONDS;
    }

    public static String getShellType() {
        return SHELL_TYPE;
    }
}
