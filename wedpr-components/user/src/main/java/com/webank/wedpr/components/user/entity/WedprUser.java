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
@TableName("wedpr_user")
@ApiModel(value = "WedprUser对象", description = "")
public class WedprUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("username")
    private String username;

    private String email;

    private String password;

    private String phone;

    private Integer tryCount = 0;

    private Long allowedTimestamp = 0L;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;

    private Integer status;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getTryCount() {
        return tryCount;
    }

    public void setTryCount(Integer tryCount) {
        this.tryCount = tryCount;
    }

    public Long getAllowedTimestamp() {
        return allowedTimestamp;
    }

    public void setAllowedTimestamp(Long allowedTimestamp) {
        this.allowedTimestamp = allowedTimestamp;
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
        return "WedprUser{"
                + "username="
                + username
                + ", email="
                + email
                + ", password="
                + password
                + ", phone="
                + phone
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
