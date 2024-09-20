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

package com.webank.wedpr.components.http.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.core.utils.BaseResponse;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonRpcResponse implements BaseResponse {
    public static class Result {
        private Integer code = 0;
        private String message;
        private String result;
        private String status;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @SneakyThrows(Exception.class)
        public String serialize() {
            return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
        }

        @Override
        public String toString() {
            return "Result{"
                    + "code="
                    + code
                    + ", message='"
                    + message
                    + '\''
                    + ", result='"
                    + result
                    + '\''
                    + ", status='"
                    + status
                    + '\''
                    + '}';
        }
    }

    private String jsonrpc;
    private Integer id;
    private Result result;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public Boolean statusOk() {
        if (this.result == null) {
            return false;
        }
        return (this.result.getCode() == 0);
    }

    public static JsonRpcResponse deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, JsonRpcResponse.class);
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "JsonRpcResponse{"
                + "jsonrpc='"
                + jsonrpc
                + '\''
                + ", id="
                + id
                + ", result="
                + result.toString()
                + '}';
    }
}
