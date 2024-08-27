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

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseShellLauncher implements ShellLauncher {
    private static final Logger logger = LoggerFactory.getLogger(BaseShellLauncher.class);
    protected final List<String> commands;
    protected final String workingDir;

    public BaseShellLauncher(List<String> commands, String workingDir) {
        this.commands = commands;
        this.workingDir = workingDir;
    }

    @Override
    public Process execute() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(new File(workingDir));
        processBuilder.command(commands);
        logger.info("Begin to execute shell command: [{}]", String.join(" ", commands));
        return processBuilder.start();
    }
}
