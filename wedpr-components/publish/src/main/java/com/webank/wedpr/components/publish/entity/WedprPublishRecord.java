package com.webank.wedpr.components.publish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zachma
 * @date 2024/8/27
 */
@TableName("wedpr_publish_record")
@ApiModel(value = "WedprPublishRecord对象", description = "")
public class WedprPublishRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String invokeId;

    private String publishId;

    private String invokeAgency;

    private String invokeUser;

    private String invokeStatus;

    private LocalDateTime invokeTime;

    private LocalDateTime applyTime;

    private LocalDateTime expireTime;

    public String getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(String invokeId) {
        this.invokeId = invokeId;
    }

    public String getPublishId() {
        return publishId;
    }

    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    public String getInvokeAgency() {
        return invokeAgency;
    }

    public void setInvokeAgency(String invokeAgency) {
        this.invokeAgency = invokeAgency;
    }

    public String getInvokeUser() {
        return invokeUser;
    }

    public void setInvokeUser(String invokeUser) {
        this.invokeUser = invokeUser;
    }

    public String getInvokeStatus() {
        return invokeStatus;
    }

    public void setInvokeStatus(String invokeStatus) {
        this.invokeStatus = invokeStatus;
    }

    public LocalDateTime getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(LocalDateTime invokeTime) {
        this.invokeTime = invokeTime;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return "WedprPublishRecord{"
                + "invokeId="
                + invokeId
                + ", publishId="
                + publishId
                + ", invokeAgency="
                + invokeAgency
                + ", invokeUser="
                + invokeUser
                + ", invokeStatus="
                + invokeStatus
                + ", invokeTime="
                + invokeTime
                + ", applyTime="
                + applyTime
                + ", expireTime="
                + expireTime
                + "}";
    }
}
