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

import com.webank.wedpr.components.task.plugin.api.model.CommandTaskConfig;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskExecutionContext;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.FileUtils;
import com.webank.wedpr.core.utils.PropertiesHelper;
import com.webank.wedpr.core.utils.ShellConstant;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseShellBuilderImpl<
                T extends BaseShellBuilderImpl<T, S>, S extends ShellLauncher>
        implements ShellBuilder<T, S> {
    private static final Logger logger = LoggerFactory.getLogger(BaseShellBuilderImpl.class);

    protected List<String> systemEnvFiles = new ArrayList<>();
    protected List<String> environmentConfigs = new ArrayList<>();
    protected List<String> scripts = new ArrayList<>();

    protected CommandTaskExecutionContext context;

    protected abstract String shellHeader();

    protected abstract String shellPostfix();

    protected abstract String shellType();

    @Override
    public T newBuilder(T builder) {
        T populatedBuilder = newBuilder();
        populatedBuilder.systemEnvFiles = builder.systemEnvFiles;
        populatedBuilder.context = builder.context;
        return populatedBuilder;
    }

    @Override
    public T context(CommandTaskExecutionContext context) {
        this.context = context;
        return (T) this;
    }

    @Override
    public T appendSystemEnv(String envFile) {
        if (StringUtils.isBlank(envFile)) {
            return (T) this;
        }
        this.systemEnvFiles.add(envFile);
        return (T) this;
    }

    @Override
    public T appendScript(String script) {
        this.scripts.add(script);
        return (T) this;
    }

    public T appendCustomEnv(String envConfig) {
        this.environmentConfigs.add(envConfig);
        return (T) this;
    }

    protected List<String> generateLaunchCommand() {
        List<String> commands = new ArrayList<>();
        if (this.context.getRunningInBackground()) {
            commands.add("nohup");
        }
        if (!this.context.getUseSudo() || !CommandTaskConfig.getEnableSudo()) {
            commands.addAll(generateLaunchCommandInNormalMode());
        } else {
            commands.addAll(generateLaunchCommandInSudoMode());
        }
        // TODO: define the output using context parameter
        if (this.context.getRunningInBackground()) {
            commands.add(
                    String.format(
                            ">%s 2>&1 &",
                            CommandTaskConfig.getOutputFile(this.context.getAppName())));
        }
        return commands;
    }

    protected Path getShellScriptPath() {
        return Paths.get(this.context.getExecutePath(), this.context.getAppName() + shellPostfix());
    }

    protected void generateShellScript() throws Exception {
        List<String> scriptContents = new ArrayList<>();
        // add shellHeader information
        scriptContents.add(shellHeader());
        // add baseDir information
        scriptContents.add(ShellConstant.BASE_DIR_CMD);
        // add system env commands
        scriptContents.addAll(generateSystemEnvScript());
        // add environment commands
        scriptContents.addAll(environmentConfigs);
        // add the shell
        scriptContents.add(generateShellContent());
        String scriptData =
                scriptContents.stream().collect(Collectors.joining(System.lineSeparator()));
        // generate and write the shell-script content
        FileUtils.createExecutableFile(getShellScriptPath());
        Files.write(getShellScriptPath(), scriptData.getBytes(), StandardOpenOption.APPEND);
        logger.info(
                "generateShellScript success, file: {}, content: {}",
                getShellScriptPath().toString(),
                scriptData);
    }

    private List<String> generateLaunchCommandInNormalMode() {
        List<String> commands = new ArrayList<>();
        commands.add(shellType());
        commands.add(getShellScriptPath().toString());
        logger.info("generateLaunchCommandInNormalMode, command: {}", String.join(" ", commands));
        return commands;
    }

    private List<String> generateLaunchCommandInSudoMode() {
        // execute command with resource limit
        List<String> commands;
        if (!PropertiesHelper.getValue(
                this.context.getParameterMap(),
                ShellConstant.RESOURCE_LIMIT_PROPERTY,
                false,
                Boolean.FALSE)) {
            commands = generateSudoModeCommandSettingWithoutResourceLimit();
        } else {
            commands = generateSudoModeCommandSettingWithResourceLimit();
        }
        commands.addAll(generateLaunchCommandInNormalMode());
        logger.info("generateLaunchCommandInSudoMode, command: {}", String.join(" ", commands));
        return commands;
    }

    private List<String> generateSudoModeCommandSettingWithResourceLimit() {
        List<String> commands = new ArrayList<>();
        commands.add(ShellConstant.RESOURCE_LIMIT_CMD);
        if (this.context.getCpuQuota() != null) {
            commands.add("-p");
            // without limit
            if (this.context.getCpuQuota() <= 0) {
                commands.add(String.format("%s=", ShellConstant.CPU_QUOTA));
            } else {
                commands.add(
                        String.format(
                                "%s=%s%%", ShellConstant.CPU_QUOTA, this.context.getCpuQuota()));
            }
        }

        if (this.context.getMaxMemory() != null) {
            commands.add("-p");
            // without limit
            if (this.context.getMaxMemory() <= 0) {
                commands.add(
                        String.format("%s=%s", ShellConstant.MEMORY_LIMIT, ShellConstant.INFINITY));
            } else {
                commands.add(
                        String.format(
                                "%s=%s", ShellConstant.MEMORY_LIMIT, this.context.getMaxMemory()));
            }
        }
        if (StringUtils.isNotBlank(this.context.getExecuteUser())) {
            commands.add(String.format("--uid=%s", this.context.getExecuteUser()));
        }
        return commands;
    }

    private List<String> generateSudoModeCommandSettingWithoutResourceLimit() {
        List<String> commands = new ArrayList<>();
        commands.add(ShellConstant.SUDO_COMMAND);
        if (StringUtils.isNotBlank(this.context.getExecuteUser())) {
            commands.add("-u");
            commands.add(this.context.getExecuteUser());
        }
        return commands;
    }

    protected List<String> generateSystemEnvScript() {
        if (systemEnvFiles.isEmpty()) {
            return Collections.emptyList();
        }
        return systemEnvFiles
                .stream()
                .map(systemEnvFile -> "source " + systemEnvFile)
                .collect(Collectors.toList());
    }

    private String generateShellContent() {
        if (scripts.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String scriptsContent =
                scripts.stream()
                        .collect(Collectors.joining(System.lineSeparator()))
                        .replaceAll(ShellConstant.WINDOWS_LINE_SPLITTER, System.lineSeparator());
        // substitutor with parameterMaps
        return Common.substitutorVarsWithParameters(scriptsContent, this.context.getParameterMap());
    }
}
