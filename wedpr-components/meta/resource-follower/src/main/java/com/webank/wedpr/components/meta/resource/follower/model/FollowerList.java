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

package com.webank.wedpr.components.meta.resource.follower.model;

import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import java.util.List;

public class FollowerList {
    private long total;
    private List<FollowerDO> dataList;

    public FollowerList() {}

    public FollowerList(long total, List<FollowerDO> dataList) {
        this.total = total;
        this.dataList = dataList;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<FollowerDO> getDataList() {
        return dataList;
    }

    public void setDataList(List<FollowerDO> dataList) {
        this.dataList = dataList;
    }
}
