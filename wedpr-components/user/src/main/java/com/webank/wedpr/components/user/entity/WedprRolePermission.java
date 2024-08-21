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
@TableName("wedpr_role_permission")
@ApiModel(value = "WedprRolePermission对象", description = "")
@Builder
public class WedprRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "role_id", type = IdType.INPUT)
    private String roleId;

    private String roleName;

    @TableField(value = "permission_id", insertStrategy = FieldStrategy.IGNORED)
    private String permissionId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
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
        return "WedprRolePermission{"
                + "roleId="
                + roleId
                + ", roleName="
                + roleName
                + ", permissionId="
                + permissionId
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
