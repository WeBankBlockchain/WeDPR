package com.webank.wedpr.components.admin.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/9/11 9:59 */
@Data
public class JobTypeStat {
    private String jobType;
    private List<String> dateList;
    private List<Integer> countList;
}