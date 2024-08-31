package com.webank.wedpr.components.publish.entity.response;

import com.webank.wedpr.components.publish.entity.WedprPublishService;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/29
 */
@Data
@AllArgsConstructor
public class WedprPublishSearchResponse {
    private long total;
    List<WedprPublishService> wedprPublishServiceList;
}
