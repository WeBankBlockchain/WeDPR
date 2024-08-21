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

import java.util.List;

public class AuthResponse {
    private String resourceID;
    private List<String> authIDList;

    public AuthResponse(String resourceID) {
        this.resourceID = resourceID;
    }

    public AuthResponse(String resourceID, List<String> authIDList) {

        this.resourceID = resourceID;
        this.authIDList = authIDList;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public List<String> getAuthIDList() {
        return authIDList;
    }

    public void setAuthIDList(List<String> authIDList) {
        this.authIDList = authIDList;
    }
}
