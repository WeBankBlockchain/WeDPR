/*
 *
 * Copyright 2017-2025 [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.webank.wedpr.components.sync.core;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.math.BigInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

/// the ResourceActionRecord used to update the resourceMeta
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceActionRecord {
    @TableField(exist = false)
    protected String user;
    // the id
    @TableId("resource_id")
    protected String resourceID = WeDPRUuidGenerator.generateID();
    // the request initiator
    protected String agency;

    // the resourceType
    protected String resourceType;

    // the resourceAction
    protected String resourceAction;
    // the resourceContent
    @TableField(exist = false)
    protected String resourceContent;
    // the recordIndex
    protected BigInteger index;
    // the blockNumber
    protected BigInteger blockNumber;
    // the blockHash
    protected String transactionHash;
    // Note: the index should not be null
    protected String trigger = "";

    @TableField(exist = false)
    protected Integer step = 1;

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceAction() {
        return resourceAction;
    }

    public void setResourceAction(String resourceAction) {
        this.resourceAction = resourceAction;
    }

    public String getResourceContent() {
        return resourceContent;
    }

    public void setResourceContent(String resourceContent) {
        this.resourceContent = resourceContent;
    }

    public BigInteger getIndex() {
        return index;
    }

    public void setIndex(BigInteger index) {
        this.index = index;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public void setTrigger(String trigger) {
        if (trigger == null) {
            return;
        }
        this.trigger = trigger;
    }

    @SneakyThrows(Exception.class)
    public String serialize() {
        return ObjectMapperFactory.getObjectMapper().writeValueAsString(this);
    }

    @SneakyThrows(Exception.class)
    public static ResourceActionRecord deserialize(String recorder) {
        return ObjectMapperFactory.getObjectMapper()
                .readValue(recorder, ResourceActionRecord.class);
    }

    @Override
    public String toString() {
        return "ResourceActionRecord{"
                + "resourceID='"
                + resourceID
                + '\''
                + ", agency='"
                + agency
                + '\''
                + ", resourceType='"
                + resourceType
                + '\''
                + ", resourceAction='"
                + resourceAction
                + '\''
                + ", resourceContent='"
                + resourceContent
                + '\''
                + ", index="
                + index
                + ", blockNumber="
                + blockNumber
                + '}';
    }
}
