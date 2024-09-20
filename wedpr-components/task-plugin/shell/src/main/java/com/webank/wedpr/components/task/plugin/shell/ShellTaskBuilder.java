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

import com.webank.wedpr.components.task.plugin.api.TaskBuilder;
import com.webank.wedpr.components.task.plugin.api.TaskInterface;
import com.webank.wedpr.core.protocol.task.TaskExecutionContext;

public class ShellTaskBuilder implements TaskBuilder {
    @Override
    public TaskInterface createTask(TaskExecutionContext context) {
        return new ShellTask(context);
    }
}
