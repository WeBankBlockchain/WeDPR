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

package com.webank.wedpr.worker.main;

import com.webank.wedpr.components.initializer.WeDPRApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PirApplication {
    private static final Logger logger = LoggerFactory.getLogger(PirApplication.class);

    public static void main(String[] args) throws Exception {
        System.out.println("start PirApplication");
        long startT = System.currentTimeMillis();
        WeDPRApplication.main(args, "WEDPR-PIR");
        System.out.println(
                "WEDPR-PIR: start PirApplication success, timecost: "
                        + (System.currentTimeMillis() - startT)
                        + " ms.");
    }
}
