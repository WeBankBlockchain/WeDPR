package com.webank.wedpr.components.admin.request;

import com.webank.wedpr.common.utils.Constant;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Data;

@Data
public class GetWedprAuditLogListRequest {
    private String ownerAgencyName;
    private String resourceAction;
    private String resourceType;
    private String status;
    private String startTime;
    private String endTime;
    private Integer pageNum = Constant.DEFAULT_PAGE_NUM;

    @Min(value = 1, message = "分页条数最小不能小于1")
    @Max(value = 10000, message = "分页条数最大不能大于10000")
    private Integer pageSize = Constant.DEFAULT_PAGE_SIZE;
}
