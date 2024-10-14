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

package com.webank.wedpr.components.uuid.generator.demo;

import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;

public class UuidGeneratorDemo {

    public static void main(String[] args) {
        Integer generatedUuidNum = 10;
        if (args.length > 0) {
            generatedUuidNum = Integer.valueOf(args[0]);
        }
        String machineID = "0";
        if (args.length > 1) {
            machineID = args[1];
        }
        WeDPRConfig.apply("wedpr.uuid.generator", "yitter");
        WeDPRConfig.apply("wedpr.uuid.generator.worker.id", machineID);

        System.out.println("======= UuidGeneratorDemo: generate uuid ========== ");
        for (int i = 0; i < generatedUuidNum.intValue(); i++) {
            System.out.println("* " + WeDPRUuidGenerator.generateID());
        }
        System.out.println("==== UuidGeneratorDemo: generate uuid done ======= ");
    }
}
