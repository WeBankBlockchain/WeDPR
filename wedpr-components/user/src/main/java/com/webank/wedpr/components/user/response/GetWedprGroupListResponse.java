package com.webank.wedpr.components.user.response;

import java.util.List;
import lombok.Data;

/** Created by caryliao on 2024/7/18 16:44 */
@Data
public class GetWedprGroupListResponse {
    private Long total;
    private List<WedprGroupDTO> groupList;
}
