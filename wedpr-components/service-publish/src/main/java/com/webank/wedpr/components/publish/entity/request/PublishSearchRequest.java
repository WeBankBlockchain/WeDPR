package com.webank.wedpr.components.publish.entity.request;

import com.webank.wedpr.components.db.mapper.service.publish.dao.PublishedServiceInfo;
import com.webank.wedpr.core.utils.PageRequest;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author zachma
 * @date 2024/8/31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
public class PublishSearchRequest extends PageRequest {
    private PublishedServiceInfo condition = new PublishedServiceInfo();
    private List<String> serviceIdList;

    public void setCondition(PublishedServiceInfo condition) {
        if (condition == null) {
            return;
        }
        this.condition = condition;
    }
}
