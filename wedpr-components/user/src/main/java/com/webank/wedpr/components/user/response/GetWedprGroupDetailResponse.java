package com.webank.wedpr.components.user.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:44 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetWedprGroupDetailResponse {
    private Long total;
    private List<WedprUserResponse> userList;
}
