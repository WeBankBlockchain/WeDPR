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

package com.webank.wedpr.components.sync.core;

public class ResourceActionRecorderBuilder {
    private final String agency;
    private final String resourceType;

    public ResourceActionRecorderBuilder(String agency, String resourceType) {
        this.agency = agency;
        this.resourceType = resourceType;
    }

    public ResourceActionRecord build(String resourceID, String resourceAction, String content) {
        ResourceActionRecord record = new ResourceActionRecord();
        record.setAgency(this.agency);
        record.setResourceType(this.resourceType);
        record.setResourceAction(resourceAction);
        record.setResourceID(resourceID);
        record.setResourceContent(content);
        return record;
    }
}
