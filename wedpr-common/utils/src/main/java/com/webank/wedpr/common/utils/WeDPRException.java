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
package com.webank.wedpr.common.utils;

import lombok.Getter;

@Getter
public class WeDPRException extends Exception {

    private int code;

    public WeDPRException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WeDPRException(String message) {
        super(message);
    }

    public WeDPRException(Throwable cause) {
        super(cause);
    }

    public WeDPRException(String message, Throwable cause) {
        super(message, cause);
    }
}
