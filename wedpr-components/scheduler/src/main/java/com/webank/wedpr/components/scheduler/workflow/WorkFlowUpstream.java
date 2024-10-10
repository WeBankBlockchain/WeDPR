package com.webank.wedpr.components.scheduler.workflow;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class WorkFlowUpstream {

    @Data
    public static class OutputInputMapEntry {
        private int output;
        private int input;
    }

    private int index = -1;
    private List<OutputInputMapEntry> outputInputMap = new ArrayList<>();
}
