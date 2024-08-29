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
@TableName("wedpr_publish")
@Data
@ApiModel(value = "WedprPublish对象", description = "")
public class WedprPublish implements Serializable {

    private static final long serialVersionUID = 1L;

    private String publishId;

    private String publishName;

    private String publishContent;

    private String datasetId;

    private String publishType;

    private String searchRule;

    private String publishUser;

    private String publishAgency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @SneakyThrows(Exception.class)
    public String serialize() {
        return JsonUtils.object2jsonString(this);
    }

    @Override
    public String toString() {
        return "WedprPublish{"
                + "publishId="
                + publishId
                + ", publishName="
                + publishName
                + ", publishContent="
                + publishContent
                + ", datasetId="
                + datasetId
                + ", publishType="
                + publishType
                + ", searchRule="
                + searchRule
                + ", publishUser="
                + publishUser
                + ", publishTime="
                + publishTime
                + "}";
    }
}
