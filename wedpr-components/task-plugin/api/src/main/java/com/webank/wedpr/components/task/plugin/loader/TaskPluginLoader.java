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
package com.webank.wedpr.components.task.plugin.loader;

import com.webank.wedpr.components.spi.plugin.SPILoader;
import com.webank.wedpr.components.task.plugin.api.TaskBuilder;
import com.webank.wedpr.components.task.plugin.api.TaskBuilderFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskPluginLoader {
    private static Logger logger = LoggerFactory.getLogger(TaskPluginLoader.class);
    private static final Map<String, TaskBuilder> taskBuilderMap = new HashMap<>();
    private static AtomicBoolean loaded = new AtomicBoolean(false);

    static {
        load();
    }

    public static void load() {
        if (!loaded.compareAndSet(false, true)) {
            logger.warn("The task plugins have already been loaded!");
            return;
        }
        SPILoader<TaskBuilderFactory> taskBuilderFactorySPILoader =
                new SPILoader<>(TaskBuilderFactory.class);
        // create the taskBuilder
        for (String name : taskBuilderFactorySPILoader.getSpiObjectMap().keySet()) {
            TaskBuilderFactory factory = taskBuilderFactorySPILoader.getSPIObjectByName(name);
            if (factory == null) {
                continue;
            }
            taskBuilderMap.put(factory.getName(), factory.createTaskBuilder());
            logger.info(
                    "register task plugin success, name: {}, classInfo: {}",
                    name,
                    factory.getClass().getSimpleName());
        }
    }

    public static Map<String, TaskBuilder> getTaskBuilderMap() {
        return taskBuilderMap;
    }

    public static TaskBuilder getTaskBuilderByName(String name) {
        if (!taskBuilderMap.containsKey(name)) {
            return null;
        }
        return taskBuilderMap.get(name);
    }
}
