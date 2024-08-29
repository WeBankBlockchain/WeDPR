package com.webank.wedpr.components.publish.entity.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zachma
 * @date 2024/8/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PublishSearchRequest extends BasePageRequest {
    private String publishName;

    private String publishAgency;

    private String publishType;

    private String publishUser;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private String publishDate;
}
