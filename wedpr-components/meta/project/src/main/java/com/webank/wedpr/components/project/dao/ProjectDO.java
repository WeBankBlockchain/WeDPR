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

package com.webank.wedpr.components.project.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ToString
public class ProjectDO extends TimeRange {
    public static enum ProjectType {
        Expert("Expert"),
        Wizard("Wizard");
        private String type;

        ProjectType(String type) {
            this.type = type;
        }

        private String getType() {
            return this.type;
        }

        public static ProjectType deserialize(String type) {
            if (StringUtils.isBlank(type)) {
                return null;
            }
            for (ProjectType projectType : ProjectType.values()) {
                if (projectType.type.compareToIgnoreCase(type) == 0) {
                    return projectType;
                }
            }
            return null;
        }
    }

    private String id = WeDPRUuidGenerator.generateID();
    private String name;
    private String projectDesc;
    private String owner;
    private String ownerAgency;
    private String label = "";
    // the jobCount
    private Long jobCount;

    private Integer reportStatus;
    private String type;
    private ProjectType projectType;
    private Integer limitItems;

    private String createTime;
    private String lastUpdateTime;

    public ProjectDO() {}

    public ProjectDO(boolean resetID) {
        if (resetID) {
            this.id = "";
        }
    }

    public ProjectDO(String name) {
        setName(name);
    }

    public void setType(String type) {
        this.type = type;
        this.projectType = ProjectType.deserialize(type);
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
        this.type = projectType.getType();
    }

    public void checkCreate() throws WeDPRException {
        Common.requireNonEmpty("projectName", name);
        Common.requireNonEmpty("projectDesc", projectDesc);
        Common.requireNonEmpty("projectType", type);
        if (projectType == null) {
            throw new WeDPRException("Invalid ProjectType: " + type);
        }
    }

    public void checkUpdate() {
        Common.requireNonEmpty("id", "projectID");
    }
}
