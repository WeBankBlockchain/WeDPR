package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListDatasetResponse {
    long totalCount;
    boolean isLast;
    List<Dataset> content;
}
