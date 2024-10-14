package com.webank.wedpr.components.dataset.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetFileShardsInfoResponse {
    int shardCount;
}
