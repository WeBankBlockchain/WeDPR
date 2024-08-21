package com.webank.wedpr.components.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@TableName("wedpr_image_code")
@ApiModel(value = "WedprImageCode对象", description = "")
@Data
@Builder
public class WedprImageCode {
    private String id;
    private String code;
    private LocalDateTime createTime;
}
