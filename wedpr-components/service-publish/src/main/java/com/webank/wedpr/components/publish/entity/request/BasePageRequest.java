package com.webank.wedpr.components.publish.entity.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.common.utils.Constant;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/31
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasePageRequest {
    private Integer pageNum = Constant.DEFAULT_PAGE_NUM;

    @Min(value = 1, message = "分页条数最小不能小于1")
    @Max(value = 10000, message = "分页条数最大不能大于10000")
    private Integer pageSize = Constant.DEFAULT_PAGE_SIZE;
}
