package com.webank.wedpr.components.publish.entity.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.db.mapper.service.publish.dao.ServiceInvokeDO;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zachma
 * @date 2024/8/31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PublishInvokeSearchRequest extends PageRequest {
    private ServiceInvokeDO condition = new ServiceInvokeDO("");

    public void check() throws Exception {
        Common.requireNonEmpty("condition.ServiceId", condition.getServiceId());
    }

    public void setCondition(ServiceInvokeDO condition) {
        if (condition == null) {
            return;
        }
        this.condition = condition;
    }
}
