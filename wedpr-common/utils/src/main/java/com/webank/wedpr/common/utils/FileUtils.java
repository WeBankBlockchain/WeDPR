/*
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */
package com.webank.wedpr.common.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FileUtils {
    public static final Set<PosixFilePermission> EXECUTABLE_PERMISSION =
            PosixFilePermissions.fromString("rwxr-xr-x");

    public static void createExecutableFile(Path path) throws Exception {
        createParentDirectory(path);
        Files.createFile(path);
        Files.setPosixFilePermissions(path, EXECUTABLE_PERMISSION);
    }

    public static void removeFile(Path path) throws Exception {
        if (!Files.exists(path)) {
            return;
        }
        Files.delete(path);
    }

    public static void createParentDirectory(Path path) throws Exception {
        if (path == null) {
            throw new WeDPRException("Must define the path!");
        }
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
    }
}
