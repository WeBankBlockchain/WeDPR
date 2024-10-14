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

public class ProjectOverviewRequest {
    private TimeRange statTime;

    public TimeRange getStatTime() {
        return statTime;
    }

    public void setStatTime(TimeRange statTime) {
        this.statTime = statTime;
    }

    public void check() throws Exception {
        if (this.statTime != null) {
            this.statTime.check();
        }
    }

    @Override
    public String toString() {
        return "ProjectOverviewRequest{" + "statTime=" + statTime + '}';
    }
}
