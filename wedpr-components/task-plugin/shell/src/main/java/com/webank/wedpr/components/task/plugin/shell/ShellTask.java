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
package com.webank.wedpr.components.task.plugin.shell;

import com.webank.wedpr.common.protocol.task.ShellParameters;
import com.webank.wedpr.common.protocol.task.TaskExecutionContext;
import com.webank.wedpr.common.protocol.task.TaskResponse;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.task.plugin.api.TaskInterface;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskExecutionContext;
import com.webank.wedpr.components.task.plugin.api.shell.ShellBuilder;
import com.webank.wedpr.components.task.plugin.api.shell.ShellBuilderFactory;
import com.webank.wedpr.components.task.plugin.api.shell.ShellCommandExecutor;
import lombok.SneakyThrows;

public class ShellTask implements TaskInterface {
    protected final CommandTaskExecutionContext context;
    protected final ShellParameters parameters;
    protected final ShellCommandExecutor executor;

    @SneakyThrows(Exception.class)
    public ShellTask(TaskExecutionContext taskExecutionContext) {
        this.context = (CommandTaskExecutionContext) taskExecutionContext;
        // parse and check the task parameter
        this.parameters = ShellParameters.deserialize(this.context.getTaskParameters());
        if (this.parameters == null) {
            throw new WeDPRException(
                    "Create ShellTask failed for invalid taskParameter: "
                            + this.context.getTaskParameters());
        }
        this.parameters.checkParameters();
        // create the executor
        this.executor = new ShellCommandExecutor(this.context);
    }

    // do nothing here
    @Override
    public void init() {}

    @SneakyThrows(Exception.class)
    @Override
    public TaskResponse run() {
        ShellBuilder shellBuilder = ShellBuilderFactory.create();
        shellBuilder.appendScript(parameters.getCode());
        return this.executor.run(shellBuilder);
    }
}
