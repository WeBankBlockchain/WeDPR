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

package com.webank.wedpr.components.mybatis;

import com.github.pagehelper.PageHelper;
import com.webank.wedpr.common.utils.PageRequest;

public class PageHelperWrapper implements AutoCloseable {
    boolean usePage = false;

    public PageHelperWrapper(PageRequest pageRequest) {
        if (pageRequest.getPageNum() == null || pageRequest.getPageSize() == null) {
            return;
        }
        if (pageRequest.getPageNum() > 0 && pageRequest.getPageSize() > 0) {
            PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
            usePage = true;
        }
    }

    @Override
    public void close() {
        if (!usePage) {
            return;
        }
        PageHelper.clearPage();
    }

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
