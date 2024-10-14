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

package com.webank.wedpr.common.protocol;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum JobStatus {
    Submitted("Submitted"),
    Running("Running"),
    RunFailed("RunFailed"),
    RunSuccess("RunSuccess"),
    WaitToRetry("WaitToRetry"),
    WaitToKill("WaitToKill"),
    Killing("Killing"),
    Killed("Killed"),
    KillFailed("KillFailed"),
    ChainInProgress("ChainInProgress");

    private static final Logger logger = LoggerFactory.getLogger(JobStatus.class);
    private final String status;

    JobStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static JobStatus deserialize(String status) {
        if (StringUtils.isBlank(status)) {
            return null;
        }
        for (JobStatus jobStatus : JobStatus.values()) {
            if (jobStatus.status.compareToIgnoreCase(status) == 0) {
                return jobStatus;
            }
        }
        return null;
    }

    public boolean finished() {
        return this.ordinal() == JobStatus.RunFailed.ordinal()
                || this.ordinal() == JobStatus.RunSuccess.ordinal()
                || this.ordinal() == Killed.ordinal()
                || this.ordinal() == KillFailed.ordinal();
    }

    public boolean success() {
        return this.ordinal() == JobStatus.RunSuccess.ordinal();
    }

    public static Boolean success(String jobStatus) {
        return (jobStatus.compareToIgnoreCase(JobStatus.RunSuccess.getStatus()) == 0);
    }
}
