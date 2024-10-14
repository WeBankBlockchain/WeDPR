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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import java.util.List;

public class FormTemplateSetting {
    public enum ColumnType {
        Selection("selection"),
        Number("number"),
        Date("date"),
        Select("select"),
        Month("month");

        private final String name;

        ColumnType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
    // the formCellOptions
    public static class FormCellOption {
        private final String value;
        private final String label;

        public FormCellOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return "FormCellOption{" + "value='" + value + '\'' + ", label='" + label + '\'' + '}';
        }
    }
    // the form column definition
    public static class FormColumnDefinition {
        private String key; // the column name
        private String title; // the column title
        private String type; // the column type, e.g. selection/number/date/select...
        private Boolean required = Boolean.FALSE;
        private String defaultValue; // the defaultValue
        private Boolean visibility = Boolean.TRUE;
        private List<FormCellOption> options;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Boolean getVisibility() {
            return visibility;
        }

        public void setVisibility(Boolean visibility) {
            this.visibility = visibility;
        }

        public List<FormCellOption> getOptions() {
            return options;
        }

        public void setOptions(List<FormCellOption> options) {
            this.options = options;
        }

        @Override
        public String toString() {
            return "FormColumnDefinition{"
                    + "key='"
                    + key
                    + '\''
                    + ", title='"
                    + title
                    + '\''
                    + ", type='"
                    + type
                    + '\''
                    + ", required="
                    + required
                    + ", defaultValue='"
                    + defaultValue
                    + '\''
                    + ", visibility="
                    + visibility
                    + ", options="
                    + options
                    + '}';
        }
    }

    private String name; // the form name
    private List<FormColumnDefinition> columns; // the column definition

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FormColumnDefinition> getColumns() {
        return columns;
    }

    public void setColumns(List<FormColumnDefinition> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "FormTemplateSetting{" + "name='" + name + '\'' + ", columns=" + columns + '}';
    }

    public String serialize() throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    public static FormTemplateSetting deserialize(String data) throws JsonProcessingException {
        return ObjectMapperFactory.getObjectMapper().readValue(data, FormTemplateSetting.class);
    }
}
