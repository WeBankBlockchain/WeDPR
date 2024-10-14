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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.utils.Common;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthChain {
    private static final Logger logger = LoggerFactory.getLogger(AuthChain.class);

    public static class AuthNode {
        protected String name;
        protected String cnName;
        protected String agency;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCnName() {
            return cnName;
        }

        public void setCnName(String cnName) {
            this.cnName = cnName;
        }

        public String getAgency() {
            return agency;
        }

        public void setAgency(String agency) {
            this.agency = agency;
        }

        public void check() {
            Common.requireNonEmpty("name", name);
            Common.requireNonEmpty("agency", agency);
        }
        // use to find the authNode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthNode authNode = (AuthNode) o;
            return name.compareToIgnoreCase(authNode.name) == 0
                    && agency.compareToIgnoreCase(authNode.agency) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, cnName, agency);
        }

        @Override
        public String toString() {
            return "AuthNode{"
                    + "name='"
                    + name
                    + '\''
                    + ", cnName='"
                    + cnName
                    + '\''
                    + ", agency='"
                    + agency
                    + '\''
                    + '}';
        }
    }
    // with order
    protected List<AuthNode> chain;

    @JsonIgnore
    private transient Map<String, Map<String, AuthNode>> authorizerToAuthNode = new HashMap<>();

    public void setChain(List<AuthNode> resultList) {
        this.chain = resultList;
        if (this.chain == null) {
            return;
        }
        // set authorizerToAuthNode
        for (AuthNode authNode : chain) {
            if (!authorizerToAuthNode.containsKey(authNode.getAgency())) {
                authorizerToAuthNode.put(authNode.getAgency(), new HashMap<>());
            }
            authorizerToAuthNode.get(authNode.getAgency()).put(authNode.getName(), authNode);
        }
    }

    public Map<String, Map<String, AuthNode>> getAuthorizerToAuthNode() {
        return authorizerToAuthNode;
    }

    public List<AuthNode> getChain() {
        return chain;
    }

    @SneakyThrows(WeDPRException.class)
    public AuthNode progressToNextNode(AuthNode currentNode) {
        if (chain == null || chain.isEmpty()) {
            throw new WeDPRException("progressToNextNode error for the authChain not set!");
        }
        int pos = chain.indexOf(currentNode);
        if (pos == -1) {
            logger.warn(
                    "progressToNextNode, the currentNode not exist in the auth-chain, currentNode: {}",
                    currentNode.toString());
            return chain.get(0);
        }
        if (chain.size() > (pos + 1)) {
            AuthNode nextNode = chain.get(pos + 1);
            logger.info(
                    "progressToNextNode, currentNode pos: {}, chainSize: {}, nextNode: {}",
                    pos,
                    chain.size(),
                    nextNode.toString());
            return nextNode;
        }
        logger.info(
                "progressToNextNode, currentNode is the last authNode, chainSize: {}, currentNode: {}",
                chain.size(),
                currentNode.toString());
        return null;
    }

    @Override
    public String toString() {
        return "AuthChain{" + "chain=" + chain + '}';
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static AuthChain deserialize(String authChain) {
        if (StringUtils.isBlank(authChain)) {
            return null;
        }
        return ObjectMapperFactory.getObjectMapper().readValue(authChain, AuthChain.class);
    }

    public void check() {
        for (AuthNode authNode : chain) {
            authNode.check();
        }
    }
}
