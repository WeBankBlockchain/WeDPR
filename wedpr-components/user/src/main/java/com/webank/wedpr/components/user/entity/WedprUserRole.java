package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * @author caryliao
 * @since 2024-07-15
 */
@TableName("wedpr_user_role")
@ApiModel(value = "WedprUserRole对象", description = "")
@Builder
public class WedprUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "username", type = IdType.INPUT)
    private String username;

    @TableField(value = "role_id", insertStrategy = FieldStrategy.IGNORED)
    private String roleId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
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

    @Override
    public String toString() {
        return "WedprUserRole{"
                + "username="
                + username
                + ", roleId="
                + roleId
                + ", createTime="
                + createTime
                + ", updateTime="
                + updateTime
                + ", createBy="
                + createBy
                + ", updateBy="
                + updateBy
                + "}";
    }
}
