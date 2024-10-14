package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@TableName("wedpr_image_code")
@ApiModel(value = "WedprImageCode对象", description = "")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class WedprImageCode {
    private String id;
    private String code;
    private LocalDateTime createTime;
}
