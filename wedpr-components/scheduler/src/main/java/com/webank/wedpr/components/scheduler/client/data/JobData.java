package com.webank.wedpr.components.scheduler.client.data;

import java.math.BigDecimal;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
public class JobData {
    private String status;

    @JsonProperty("time_costs")
    private BigDecimal timeCosts;
}
