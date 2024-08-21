package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author caryliao
 * @since 2024-07-15
 */
@TableName("wedpr_group_detail")
@ApiModel(value = "WedprGroupDetail对象", description = "")
public class WedprGroupDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupId;

    private String username;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
        return "WedprGroupDetail{"
                + "groupId="
                + groupId
                + ", username="
                + username
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
