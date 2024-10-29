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

package com.webank.wedpr.components.authorization.model;

import org.apache.commons.lang3.StringUtils;

public enum AuthResultStatus {
    Submit("Submit"),
    Agree("Agree"),
    Reject("Reject"),
    Cancel("Cancel");

    private final String name;

    AuthResultStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean agree() {
        return this.ordinal() == Agree.ordinal();
    }

    public boolean reject() {
        return this.ordinal() == Reject.ordinal();
    }

    public boolean cancel() {
        return this.ordinal() == Cancel.ordinal();
    }

    public static AuthResultStatus deserialize(String result) {
        if (StringUtils.isBlank(result)) {
            return null;
        }
        for (AuthResultStatus authResultStatus : AuthResultStatus.values()) {
            if (authResultStatus.name.compareToIgnoreCase(result) == 0) {
                return authResultStatus;
            }
        }
        return null;
    }
}
