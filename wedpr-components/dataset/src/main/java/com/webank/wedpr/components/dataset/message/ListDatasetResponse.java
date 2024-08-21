package com.webank.wedpr.components.dataset.message;

import com.webank.wedpr.components.dataset.dao.Dataset;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ListDatasetResponse {
    int totalCount;
    boolean isLast;
    List<Dataset> content;
}
