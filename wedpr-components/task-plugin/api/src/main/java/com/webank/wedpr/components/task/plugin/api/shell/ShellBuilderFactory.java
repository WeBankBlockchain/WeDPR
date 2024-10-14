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
package com.webank.wedpr.components.task.plugin.api.shell;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskConfig;
import com.webank.wedpr.components.task.plugin.api.shell.bash.BashBuilder;
import lombok.SneakyThrows;

public class ShellBuilderFactory {
    private static ShellType shellType;

    static {
        loadShellType();
    }

    @SneakyThrows(Exception.class)
    private static void loadShellType() {
        shellType = ShellType.deserialize(CommandTaskConfig.getShellType());
        if (shellType == null) {
            throw new WeDPRException(
                    "Not supported shell type " + CommandTaskConfig.getShellType());
        }
    }

    @SneakyThrows(Exception.class)
    public static ShellBuilder create() {
        if (shellType.equals(ShellType.BASH)) {
            return new BashBuilder();
        }
        throw new WeDPRException(
                "Create ShellBuilder failed for not support shell type: " + shellType.getType());
    }
}
