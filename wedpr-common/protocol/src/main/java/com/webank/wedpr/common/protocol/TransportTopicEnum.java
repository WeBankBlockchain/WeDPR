package com.webank.wedpr.common.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TransportTopicEnum {

    /** project report */
    PROJECT_REPORT,

    /** job report */
    JOB_REPORT,

    /** job dataset report */
    JOB_DATASET_REPORT,

    /** sys config report */
    SYS_CONFIG_REPORT;
}
