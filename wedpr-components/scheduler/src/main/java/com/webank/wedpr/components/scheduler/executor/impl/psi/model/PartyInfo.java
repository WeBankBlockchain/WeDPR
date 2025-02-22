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

package com.webank.wedpr.components.scheduler.executor.impl.psi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyInfo {
    public static enum PartyType {
        CLIENT(0),
        SERVER(1);

        private final Integer type;

        PartyType(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return this.type;
        }
    }

    public static enum MultiPSIPartyType {
        CALCULATOR(0),
        PARTNER(1),
        MASTER(2);

        private final Integer type;

        MultiPSIPartyType(Integer type) {
            this.type = type;
        }

        public Integer getType() {
            return this.type;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PartyData {
        private String id;
        private FileMeta input;
        private FileMeta output;

        public PartyData(String id, FileMeta input, FileMeta output) {
            this.id = id;
            this.input = input;
            this.output = output;
        }

        @Override
        public String toString() {
            return "PartyData{"
                    + "id='"
                    + id
                    + '\''
                    + ", input="
                    + input
                    + ", output="
                    + output
                    + '}';
        }
    }

    private String id;
    private Integer partyIndex;
    private PartyData data;

    public PartyInfo(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PartyInfo{"
                + "id='"
                + id
                + '\''
                + ", partyIndex="
                + partyIndex
                + ", data="
                + data
                + '}';
    }
}
