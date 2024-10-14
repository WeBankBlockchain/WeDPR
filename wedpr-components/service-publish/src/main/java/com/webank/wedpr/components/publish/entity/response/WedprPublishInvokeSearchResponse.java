package com.webank.wedpr.components.publish.entity.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeDO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zachma
 * @date 2024/8/31
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class WedprPublishInvokeSearchResponse {
    private long total;
    List<ServiceInvokeDO> wedprPublishInvokeList;
}
