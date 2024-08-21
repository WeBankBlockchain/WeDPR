package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author caryliao
 * @since 2024-07-15
 */
@TableName("wedpr_group")
@ApiModel(value = "WedprGroup对象", description = "")
public class WedprGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("group_id")
    private String groupId;

    private String groupName;

    private String adminName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer status;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WedprGroup{"
                + "groupId="
                + groupId
                + ", groupName="
                + groupName
                + ", adminName="
                + adminName
                + ", createTime="
                + createTime
                + ", updateTime="
                + updateTime
                + ", createBy="
                + createBy
                + ", updateBy="
                + updateBy
                + ", status="
                + status
                + "}";
    }
}
