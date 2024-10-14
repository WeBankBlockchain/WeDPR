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

public class JobOverviewResponse {
    public static class JobOverview {
        private String jobType;
        private Long count;

        public JobOverview() {}

        public JobOverview(String jobType, Long count) {
            this.jobType = jobType;
            this.count = count;
        }

        public String getJobType() {
            return jobType;
        }

        public void setJobType(String jobType) {
            this.jobType = jobType;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return "JobOverview{" + "jobType='" + jobType + '\'' + ", count=" + count + '}';
        }
    }

    public static class JobStat {
        private TimeRange timeRange;
        private Long count;
        private List<JobTypeStat> jobTypeStats = new ArrayList<>();

        public static class JobTypeStat {
            private String jobType;
            private Long count;

            public JobTypeStat() {}

            public JobTypeStat(String jobType, Long count) {
                this.jobType = jobType;
                this.count = count;
            }

            public String getJobType() {
                return jobType;
            }

            public void setJobType(String jobType) {
                this.jobType = jobType;
            }

            public Long getCount() {
                return count;
            }

            public void setCount(Long count) {
                this.count = count;
            }
        }

        public JobStat() {}

        public JobStat(TimeRange timeRange, Long count) {
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

        public List<JobTypeStat> getJobTypeStats() {
            return jobTypeStats;
        }

        public void setJobTypeStats(List<JobTypeStat> jobTypeStats) {
            if (jobTypeStats == null) {
                return;
            }
            this.jobTypeStats = jobTypeStats;
        }

        @Override
        public String toString() {
            return "JobStat{" + "timeRange='" + timeRange + '\'' + ", count=" + count + '}';
        }
    }

    private Long totalCount;
    private List<JobOverview> jobOverviewList = new ArrayList<>();
    private List<JobStat> statResults = new ArrayList<>();

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public List<JobOverview> getJobOverviewList() {
        return jobOverviewList;
    }

    public void setJobOverviewList(List<JobOverview> jobOverviewList) {
        if (jobOverviewList == null) {
            return;
        }
        this.jobOverviewList = jobOverviewList;
    }

    public List<JobStat> getStatResults() {
        return statResults;
    }

    public void setStatResults(List<JobStat> statResults) {
        if (statResults == null) {
            return;
        }
        this.statResults = statResults;
    }

    @Override
    public String toString() {
        return "JobOverviewResponse{"
                + "totalCount="
                + totalCount
                + ", jobOverviewList="
                + jobOverviewList
                + ", statResults="
                + statResults
                + '}';
    }
}
