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
package com.webank.wedpr.components.task.plugin.api;

import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskConfig;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskExecutionContext;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskResponse;
import com.webank.wedpr.components.task.plugin.api.shell.ShellBuilder;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandExecutor implements WorkerExecutor {
    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    protected final CommandTaskExecutionContext context;

    public CommandExecutor(CommandTaskExecutionContext taskExecutionContext) {
        this.context = taskExecutionContext;
    }

    @Override
    public CommandTaskResponse run(Object builder) throws Exception {
        ShellBuilder shellBuilder = (ShellBuilder) builder;
        // set the builder information
        shellBuilder.context(context);
        // set the system envs
        if (!CommandTaskConfig.getSystemEnvFiles().isEmpty()) {
            CommandTaskConfig.getSystemEnvFiles().forEach(shellBuilder::appendSystemEnv);
        }
        // set the environment
        if (StringUtils.isNotBlank(context.getEnvironmentConfig())) {
            shellBuilder.appendCustomEnv(context.getEnvironmentConfig());
        }
        long remainTime = -1;
        if (this.context.getTaskTimeoutMs() > 0) {
            remainTime = (System.currentTimeMillis() - this.context.getStartTime());
            if (remainTime < 0) {
                throw new WeDPRException("task execution timeout");
            }
        }
        // build the launcher
        CommandTaskResponse taskResponse = new CommandTaskResponse(this.context.getTaskID());
        Process process = shellBuilder.build().execute();
        taskResponse.setProcess(process);
        this.context.setProcess(process);
        int processId = Common.getProcessId(process);
        taskResponse.setProcessId(processId);
        this.context.setProcessId(processId);

        logger.info("bootstrap process start, process id: {}", processId);
        // wait for finish
        boolean exitNormally = Boolean.FALSE;
        if (remainTime > 0) {
            exitNormally = process.waitFor(remainTime, TimeUnit.MILLISECONDS);
        } else {
            exitNormally = (process.waitFor() == 0 ? true : false);
        }
        if (exitNormally) {
            taskResponse.setExitCode(process.exitValue());
        } else {
            // kill the command
            logger.error(
                    "process has failure, over the task timeout configuration {}, processId: {}, ready to kill",
                    context.getTaskTimeoutMs(),
                    processId);
            kill();
            taskResponse.setExitCode(Constant.WEDPR_FAILED);
        }
        logger.info(
                "execute process finished, executePath: {}, process: {}, exitCode: {}, exitNormally: {}",
                this.context.getExecutePath(),
                processId,
                process.exitValue(),
                exitNormally);
        return taskResponse;
    }

    @Override
    public void kill() throws Exception {
        if (this.context.getProcess() == null) {
            return;
        }
        logger.info("Ready to kill process: {}", this.context.getProcessId());
        this.context.getProcess().destroy();
        if (!this.context
                .getProcess()
                .waitFor(CommandTaskConfig.getKillDefaultTimeoutSeconds(), TimeUnit.SECONDS)) {
            this.context.getProcess().destroyForcibly();
        }
        logger.info("Success kill process: {}", this.context.getProcessId());
    }
}
