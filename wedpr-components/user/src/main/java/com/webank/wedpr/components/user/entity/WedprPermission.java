package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("wedpr_permission")
@ApiModel(value = "WedprPermission对象", description = "")
@Builder
public class WedprPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.INPUT)
    private String permissionId;

    private String permissionName;

    private String permissionContent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionContent() {
        return permissionContent;
    }

    public void setPermissionContent(String permissionContent) {
        this.permissionContent = permissionContent;
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
        return "WedprPermission{"
                + "permissionId="
                + permissionId
                + ", permissionName="
                + permissionName
                + ", permissionContent="
                + permissionContent
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
