package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportStatusEnum {

    /** 未上报 */
    NO_REPORT(0),

    /** 已上报 */
    DONE_REPORT(1);

    private Integer reportStatus;
}
