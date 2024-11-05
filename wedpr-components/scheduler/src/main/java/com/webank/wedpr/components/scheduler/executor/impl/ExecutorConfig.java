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

package com.webank.wedpr.components.scheduler.executor.impl;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.config.WeDPRConfig;
import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.components.pir.sdk.config.PirSDKConfig;
import java.io.File;

public class ExecutorConfig {
    private static final String JOB_CACHE_DIR =
            WeDPRConfig.apply("wedpr.executor.job.cache.dir", "./.cache/jobs");

    private static String PSI_TMP_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.psi.tmp.file.name", "psi_prepare.csv");
    private static String PSI_PREPARE_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.psi.prepare.file.name", "psi_prepare.csv");

    private static String PSI_RESULT_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.psi.result.file.name", "psi_result.csv");

    private static String MPC_PREPARE_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.mpc.prepare.file.name", "mpc_prepare.csv");

    private static String MPC_RESULT_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.mpc.result.file.name", "mpc_result.csv");

    private static String MPC_OUTPUT_FILE =
            WeDPRConfig.apply("wedpr.executor.mpc.output.file.name", "mpc_output.txt");

    private static String MPC_PSI_OPTION_REGEX =
            WeDPRConfig.apply("wedpr.executor.mpc.psi.option.regex", "PSI_OPTION\\s*=\\s*True");

    private static String MPC_CODE_GENERATOR_SCRIPT_PATH =
            WeDPRConfig.apply(
                    "wedpr.executor.mpc.code.generator.script.path",
                    "./mpc_generator/mpc_generator_main.py");

    private static String MPC_CODE_GENERATOR_ENV_PATH =
            WeDPRConfig.apply("wedpr.executor.mpc.code.generator.python.env", "");

    private static String PIR_RESULT_FILE_NAME =
            WeDPRConfig.apply("wedpr.executor.pir.result.file.name", "pir_result");

    public static String getJobCacheDir() {
        return JOB_CACHE_DIR;
    }

    public static String getMpcPsiOptionRegex() {
        return MPC_PSI_OPTION_REGEX;
    }

    public static String getMpcCodeGeneratorEnvPath() {
        return MPC_CODE_GENERATOR_ENV_PATH;
    }

    public static String getMpcCodeGeneratorScriptPath() {
        return MPC_CODE_GENERATOR_SCRIPT_PATH;
    }

    public static String getJobCacheDir(String jobID) {
        return JOB_CACHE_DIR + File.separator + jobID;
    }

    public static String getPsiTmpFileName() {
        return PSI_TMP_FILE_NAME;
    }

    public static String getPsiTmpFilePath(String jobID) {
        return Common.joinPath(ExecutorConfig.getJobCacheDir(jobID), getPsiTmpFileName());
    }

    public static String getPsiPrepareFilePath(String jobID) {
        return Common.joinPath(jobID, PSI_PREPARE_FILE_NAME);
    }

    public static String getPsiPrepareFileName() {
        return PSI_PREPARE_FILE_NAME;
    }

    public static String getDefaultPSIResultPath(String user, String jobID) {
        return WeDPRCommonConfig.getUserJobCachePath(
                user, JobType.PIR.getType(), jobID, PSI_RESULT_FILE_NAME);
    }

    public static String getMpcResultFileName() {
        return MPC_RESULT_FILE_NAME;
    }

    public static String getPsiResultFileName() {
        return PSI_RESULT_FILE_NAME;
    }

    public static String getPirJobResultPath(String user, String jobID) {
        return Common.joinPath(
                Common.joinPath(PirSDKConfig.getPirCacheDir(), Common.joinPath(user, jobID)),
                getPirResultFileName());
    }

    public static String getPirResultFileName() {
        return PIR_RESULT_FILE_NAME;
    }

    public static String getMpcPrepareFileName() {
        return MPC_PREPARE_FILE_NAME;
    }
}
