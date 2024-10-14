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

package com.webank.wedpr.components.project.model;

import com.webank.wedpr.common.utils.TimeRange;
import java.util.ArrayList;
import java.util.List;

public class ProjectOverviewResponse {
    public static class ProjectStat {
        private TimeRange timeRange;
        private Long count;

        public ProjectStat() {}

        public ProjectStat(TimeRange timeRange, Long count) {
            this.timeRange = timeRange;
            this.count = count;
        }

        public TimeRange getTimeRange() {
            return timeRange;
        }

        public void setTimeRange(TimeRange timeRange) {
            this.timeRange = timeRange;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "ProjectStat{" + "timeRange='" + timeRange + '\'' + ", count=" + count + '}';
        }
    }

    private Long totalCount;
    private List<ProjectStat> projectStatList = new ArrayList<>();

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public List<ProjectStat> getProjectStatList() {
        return projectStatList;
    }

    public void setProjectStatList(List<ProjectStat> projectStatList) {
        if (projectStatList == null) {
            return;
        }
        this.projectStatList = projectStatList;
    }

    @Override
    public String toString() {
        return "ProjectOverviewResponse{"
                + "totalCount="
                + totalCount
                + ", projectStatList="
                + projectStatList
                + '}';
    }
}
