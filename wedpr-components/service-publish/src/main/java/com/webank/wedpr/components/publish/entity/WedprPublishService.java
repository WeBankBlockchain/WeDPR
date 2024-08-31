package com.webank.wedpr.components.publish.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.webank.wedpr.components.publish.utils.JsonUtils;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.SneakyThrows;

/**
 * @author zachma
 * @since 2024-08-28
 */
@TableName("wedpr_publish_service")
@Data
@ApiModel(value = "WedprPublishService对象", description = "")
public class WedprPublishService implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceId;

    private String serviceName;

    private String serviceDesc;

    private String serviceType;

    private String searchConfig;

    private String owner;

    private String agency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @SneakyThrows(Exception.class)
    public String serialize() {
        return JsonUtils.object2jsonString(this);
    }

}
