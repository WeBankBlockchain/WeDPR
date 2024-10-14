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

package com.webank.wedpr.components.authorization.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.utils.TimeRange;
import com.webank.wedpr.components.authorization.model.AuthChain;
import com.webank.wedpr.components.authorization.model.AuthResult;
import com.webank.wedpr.components.meta.resource.follower.dao.FollowerDO;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationDO extends TimeRange {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationDO.class);

    public static enum AuthStatus {
        ToConfirm("ToConfirm"),
        Approving("Approving"),
        ApproveFailed("ApproveFailed"),
        ApproveRejected("ApproveRejected"),
        ApproveCanceled("ApproveCanceled"),
        ApproveSuccess("ApproveSuccess"),
        Progressing("Progressing"),
        ProgressFailed("ProgressFailed"),
        ProgressSuccess("ProgressSuccess"),
        Unknown("Unknown");

        private final String status;

        AuthStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return this.status;
        }

        public boolean readOnly() {
            return this.ordinal() == ApproveFailed.ordinal()
                    || this.ordinal() == ApproveCanceled.ordinal()
                    || this.ordinal() == ProgressFailed.ordinal()
                    || this.ordinal() == ProgressSuccess.ordinal();
        }

        public boolean progressing() {
            return this.ordinal() == Progressing.ordinal();
        }

        public boolean approving() {
            return this.ordinal() == Approving.ordinal() || this.ordinal() == ToConfirm.ordinal();
        }

        public boolean approveSuccess() {
            return this.ordinal() == ApproveSuccess.ordinal();
        }

        public boolean toConfirmed() {
            return this.ordinal() == ToConfirm.ordinal();
        }

        public boolean rejected() {
            return this.ordinal() == ApproveRejected.ordinal();
        }

        public static AuthStatus deserialize(String status) {
            if (StringUtils.isBlank(status)) {
                return null;
            }
            for (AuthStatus authStatus : AuthStatus.values()) {
                if (authStatus.status.compareToIgnoreCase(status) == 0) {
                    return authStatus;
                }
            }
            return null;
        }
    }

    private String id = WeDPRUuidGenerator.generateID(); // the id
    private String applicant;
    private String applicantAgency;
    private String applyType;
    private String applyTitle;
    private String applyDesc;
    private String applyChain; // json
    private AuthChain authChain;

    private String applyContent; // json
    private String applyTemplateName;
    private String status = "";
    @JsonIgnore private transient AuthStatus authStatus;

    // the approval result of all auth-node
    private String result;
    private AuthResult authResult = new AuthResult();

    // the followers
    private List<String> followers = new ArrayList<>();
    // Note: should serialize
    private List<FollowerDO> followerDOList = new ArrayList<>();

    private String currentApplyNode;
    private String currentApplyNodeAgency;
    private String executeResult;
    private String createTime;
    private String lastUpdateTime;
    // used to query the auth list according to id-list
    @JsonIgnore private transient List<String> authIDList;

    // the auth-status list used as the query condition
    private List<String> authStatusList;

    public AuthorizationDO() {}

    public AuthorizationDO(boolean enforceEmptyID) {
        if (enforceEmptyID) {
            this.id = "";
        }
    }

    public AuthorizationDO(String id) {
        setId(id);
    }

    public AuthorizationDO(String id, String status) {
        setId(id);
        setStatus(status);
    }

    public AuthorizationDO(
            String id, String currentApplyNode, String currentApplyNodeAgency, String result) {
        setId(id);
        setCurrentApplyNode(currentApplyNode);
        setCurrentApplyNodeAgency(currentApplyNodeAgency);
        setResult(result);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getApplicantAgency() {
        return applicantAgency;
    }

    public void setApplicantAgency(String applicantAgency) {
        this.applicantAgency = applicantAgency;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public String getApplyTitle() {
        return applyTitle;
    }

    public void setApplyTitle(String applyTitle) {
        this.applyTitle = applyTitle;
    }

    public String getApplyDesc() {
        return applyDesc;
    }

    public void setApplyDesc(String applyDesc) {
        this.applyDesc = applyDesc;
    }

    public String getApplyChain() {
        return applyChain;
    }

    public List<String> getAuthIDList() {
        return authIDList;
    }

    public void setAuthIDList(List<String> authIDList) {
        this.authIDList = authIDList;
    }

    public void setApplyChain(String applyChain) {
        this.applyChain = applyChain;
        if (StringUtils.isBlank(applyChain)) {
            return;
        }
        this.authChain = AuthChain.deserialize(applyChain);
    }

    public AuthChain getAuthChain() {
        return authChain;
    }

    public void setAuthChain(AuthChain authChain) {
        this.authChain = authChain;
        if (authChain == null) {
            return;
        }
        this.applyChain = this.authChain.serialize();
    }

    public String getApplyContent() {
        return applyContent;
    }

    public void setApplyContent(String applyContent) {
        this.applyContent = applyContent;
    }

    public String getApplyTemplateName() {
        return applyTemplateName;
    }

    public void setApplyTemplateName(String applyTemplateName) {
        this.applyTemplateName = applyTemplateName;
    }

    public String getStatus() {
        return status;
    }

    public AuthStatus getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(AuthStatus authStatus) {
        this.authStatus = authStatus;
        if (authStatus == null) {
            return;
        }
        setStatus(authStatus.getStatus());
    }

    public void setStatus(String status) {
        if (status == null) {
            return;
        }
        this.status = status;
        // deserialize the authStatus
        this.authStatus = AuthStatus.deserialize(status);
    }

    public String getCurrentApplyNode() {
        return currentApplyNode;
    }

    public void setCurrentApplyNode(String currentApplyNode) {
        this.currentApplyNode = currentApplyNode;
    }

    public String getCurrentApplyNodeAgency() {
        return currentApplyNodeAgency;
    }

    public void setCurrentApplyNodeAgency(String currentApplyNodeAgency) {
        this.currentApplyNodeAgency = currentApplyNodeAgency;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
        if (StringUtils.isBlank(result)) {
            return;
        }
        this.authResult = AuthResult.deserialize(result);
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    public void setAuthResult(AuthResult authResult) {
        // Note: the authResult will be serialized when sync to all related agencies
        if (authResult == null
                || authResult.getResultList() == null
                || authResult.getResultList().isEmpty()) {
            return;
        }
        this.authResult = authResult;
        this.result = authResult.serialize();
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
        this.followerDOList.clear();
        for (String follower : this.followers) {
            this.followerDOList.add(
                    new FollowerDO(
                            follower,
                            WeDPRCommonConfig.getAgency(),
                            this.id,
                            FollowerDO.FollowerType.AUTH_FOLLOWER.getType()));
        }
    }

    public List<FollowerDO> getFollowerDOList() {
        return followerDOList;
    }

    public void setFollowerDOList(List<FollowerDO> followerDOList) {
        this.followerDOList = followerDOList;
        if (this.followerDOList != null) {
            this.followers.clear();
            for (FollowerDO followerDO : this.followerDOList) {
                this.followers.add(followerDO.getUserName());
            }
        }
    }

    public List<String> getAuthStatusList() {
        return authStatusList;
    }

    public void setAuthStatusList(List<String> authStatusList) {
        this.authStatusList = authStatusList;
    }

    public String getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(String executeResult) {
        this.executeResult = executeResult;
    }

    // update the auth-result
    public void updateResult(String authorizer, AuthResult.AuthResultDetail detail) {
        this.getAuthResult().updateResult(authChain, authorizer, detail);
        // the result is rejected
        if (detail.getAuthResultStatus().reject()) {
            setStatus(AuthStatus.ApproveRejected.getStatus());
            logger.info("updateResult, set the status to {}, auth: {}", getStatus(), id);
        }
        this.result = authResult.serialize();
    }

    public void progressToNextAuthNode() {
        logger.info(
                "progressToNextAuthNode, currentNode: {}, currentNodeAgency: {}",
                currentApplyNode,
                currentApplyNodeAgency);
        AuthChain.AuthNode currentNode = new AuthChain.AuthNode();
        currentNode.setName(currentApplyNode);
        currentNode.setAgency(currentApplyNodeAgency);
        AuthChain.AuthNode authNode = this.getAuthChain().progressToNextNode(currentNode);
        if (authNode == null) {
            logger.info(
                    "progressToNextAuthNode, find the last authNode, setStatus to ApproveSuccess, auth: {}",
                    id);
            setStatus(AuthStatus.ApproveSuccess.getStatus());
            return;
        }
        logger.info(
                "progressToNextAuthNode, find the next authNode: {}, currentAuthNode: {}",
                authNode.toString(),
                currentApplyNode);
        setCurrentApplyNode(authNode.getName());
        setCurrentApplyNodeAgency(authNode.getAgency());
    }

    public void progressToApplicant(String applicant, String applicantAgency) {
        logger.info(
                "progressToApplicant, applicant: {}, applicantAgency: {}, auth: {}",
                applicant,
                applicantAgency,
                id);
        setCurrentApplyNode(applicant);
        setCurrentApplyNodeAgency(applicantAgency);
    }

    @Override
    public String toString() {
        return "AuthorizationDO{"
                + "id='"
                + id
                + '\''
                + ", applicant='"
                + applicant
                + '\''
                + ", applicantAgency='"
                + applicantAgency
                + '\''
                + ", applyType='"
                + applyType
                + '\''
                + ", applyTitle='"
                + applyTitle
                + '\''
                + ", applyDesc='"
                + applyDesc
                + '\''
                + ", applyChain='"
                + applyChain
                + '\''
                + ", applyContent='"
                + applyContent
                + '\''
                + ", applyTemplateName='"
                + applyTemplateName
                + '\''
                + ", status='"
                + status
                + '\''
                + ", currentApplyNode='"
                + currentApplyNode
                + '\''
                + ", currentApplyNodeAgency='"
                + currentApplyNodeAgency
                + '\''
                + ", createTime='"
                + createTime
                + '\''
                + ", lastUpdateTime='"
                + lastUpdateTime
                + '\''
                + ", result='"
                + result
                + '\''
                + '}';
    }
}
