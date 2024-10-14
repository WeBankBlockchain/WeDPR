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

package com.webank.wedpr.components.scheduler.executor;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class ExecuteResult {
    public enum ResultStatus {
        RUNNING("Running"),
        SUCCESS("SUCCESS"),
        FAILED("Failed");
        private final String status;

        ResultStatus(String status) {
            this.status = status;
        }

        private String getStatus() {
            return status;
        }

        public boolean finished() {
            return this.ordinal() != RUNNING.ordinal();
        }

        public boolean failed() {
            return this.ordinal() == FAILED.ordinal();
        }

        public boolean success() {
            return this.ordinal() == SUCCESS.ordinal();
        }
    }

    private String msg;
    private ResultStatus resultStatus;

    public ExecuteResult() {}

    public ExecuteResult(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public ExecuteResult(String msg, ResultStatus resultStatus) {
        this.msg = msg;
        this.resultStatus = resultStatus;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public boolean finished() {
        return this.resultStatus.finished();
    }

    // {"msg":"task is running","resultStatus":"SUCCESS"}
    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
