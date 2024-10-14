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
package com.webank.wedpr.components.task.plugin.api.shell.bash;

import com.webank.wedpr.common.utils.ShellConstant;
import com.webank.wedpr.components.task.plugin.api.shell.BaseShellBuilderImpl;
import java.util.List;
import lombok.SneakyThrows;

public class BashBuilder extends BaseShellBuilderImpl<BashBuilder, BashLauncher> {
    @Override
    public BashBuilder newBuilder() {
        return new BashBuilder();
    }

    @SneakyThrows(Exception.class)
    @Override
    public BashLauncher build() {
        generateShellScript();
        List<String> commands = generateLaunchCommand();
        return new BashLauncher(commands, this.context.getExecutePath());
    }

    @Override
    protected String shellHeader() {
        return ShellConstant.BASH_HEADER;
    }

    @Override
    protected String shellPostfix() {
        return ShellConstant.SHELL_POSTFIX;
    }

    @Override
    protected String shellType() {
        return "bash";
    }
}
