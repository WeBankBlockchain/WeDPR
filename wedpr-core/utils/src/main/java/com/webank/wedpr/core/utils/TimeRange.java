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

package com.webank.wedpr.core.utils;

import java.time.LocalDateTime;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TimeRange implements BaseRequest {
    protected String startTime;
    protected String endTime;
    protected Integer step = 1;

    private transient LocalDateTime startDate = null;
    private transient LocalDateTime endDate = LocalDateTime.now();

    public TimeRange() {}

    public TimeRange(String startTime, String endTime, Integer step) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.step = step;
    }

    public String getStartTime() {
        return startTime;
    }

    @SneakyThrows(Exception.class)
    public void setStartTime(String startTime) {
        this.startTime = startTime;
        if (Common.isNullStr(startTime)) {
            return;
        }
        this.startDate = Common.toDate(startTime);
    }

    public String getEndTime() {
        return endTime;
    }

    @SneakyThrows(Exception.class)
    public void setEndTime(String endTime) {
        if (Common.isNullStr(endTime)) {
            return;
        }
        this.endTime = endTime;
        this.endDate = Common.toDate(this.endTime);
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        if (step == null) {
            return;
        }
        this.step = step;
    }

    public void check() throws WeDPRException {
        if (this.startDate == null) {
            throw new WeDPRException("Invalid TimeRange, must set the startTime!");
        }
        if (this.endDate.isBefore(this.startDate)) {
            throw new WeDPRException(
                    "Invalid TimeRange, the endTime must no before than startTime!");
        }
    }

    public Pair<String, String> getNextTime(long stepNum) {
        LocalDateTime startT = startDate.plusDays(stepNum * step + 1);
        if (startT.isAfter(this.endDate)) {
            return null;
        }
        LocalDateTime endT = startDate.plusDays(stepNum * step + step);
        return new ImmutablePair<>(Common.dateToString(startT), Common.dateToString(endT));
    }

    @Override
    public String toString() {
        return "TimeRange{"
                + "startTime='"
                + startTime
                + '\''
                + ", endTime='"
                + endTime
                + '\''
                + ", step="
                + step
                + '}';
    }

    @Override
    public String serialize() throws Exception {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
