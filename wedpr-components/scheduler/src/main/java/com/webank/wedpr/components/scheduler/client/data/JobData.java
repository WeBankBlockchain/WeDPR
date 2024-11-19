package com.webank.wedpr.components.scheduler.client.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class JobData {
    private String status;

    @JsonProperty("time_costs")
    private BigDecimal timeCosts;
}
