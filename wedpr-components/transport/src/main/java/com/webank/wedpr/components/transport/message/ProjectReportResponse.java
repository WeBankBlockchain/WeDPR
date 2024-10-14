package com.webank.wedpr.components.transport.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/** Created by caryliao on 2024/9/5 11:28 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ProjectReportResponse {
    private Integer code;
    private String msg;
    private List<String> projectIdList;
}
