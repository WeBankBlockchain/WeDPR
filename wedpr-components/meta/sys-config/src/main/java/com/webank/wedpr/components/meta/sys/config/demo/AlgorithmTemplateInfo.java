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

package com.webank.wedpr.components.meta.sys.config.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class AlgorithmTemplateInfo {
    public static class AlgorithmTemplate {
        private String name;
        private String title;
        private String detail;
        private String version = "1.0";

        public AlgorithmTemplate() {}

        public AlgorithmTemplate(String name, String title, String detail) {
            setName(name);
            setTitle(title);
            setDetail(detail);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            if (StringUtils.isBlank(version)) {
                return;
            }
            this.version = version;
        }
    }

    String version = "1.0";

    private List<AlgorithmTemplate> templates;

    public List<AlgorithmTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<AlgorithmTemplate> templates) {
        this.templates = templates;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }
}
