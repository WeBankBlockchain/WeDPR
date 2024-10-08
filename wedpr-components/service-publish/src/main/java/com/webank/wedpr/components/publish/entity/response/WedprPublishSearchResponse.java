package com.webank.wedpr.components.publish.entity.response;

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zachma
 * @date 2024/8/31
 */
@Data
@AllArgsConstructor
public class WedprPublishSearchResponse {
    private long total;
    List<PublishedServiceInfo> wedprPublishedServiceList;
}
