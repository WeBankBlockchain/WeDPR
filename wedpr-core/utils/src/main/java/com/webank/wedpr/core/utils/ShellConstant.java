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

package com.webank.wedpr.core.utils;

public class ShellConstant {
    public static String BASE_DIR_CMD = "BASEDIR=$(cd `dirname $0`; pwd) && cd ${BASEDIR}";
    public static String WINDOWS_LINE_SPLITTER = "\\r\\n";
    public static String RESOURCE_LIMIT_PROPERTY = "resource.limit.enable";
    public static String SUDO_COMMAND = "sudo";
    public static String RESOURCE_LIMIT_CMD = "sudo systemd-run -q --scope";
    public static String CPU_QUOTA = "CPUQuota";
    public static String MEMORY_LIMIT = "MemoryLimit";
    public static String INFINITY = "infinity";
    public static String SHELL_POSTFIX = ".sh";

    public static String BASH_HEADER = "#!/bin/bash";
}
