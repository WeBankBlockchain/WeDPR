package com.webank.wedpr.components.scheduler.workflow;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class WorkFlowNode {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowNode.class);

    private int index;
    private List<WorkFlowUpstream> upstreams = new ArrayList<>();

    private String type;
    private List<String> args;

    public void addUpstream(int upstream) {
        WorkFlowUpstream workflowUpstream = new WorkFlowUpstream();
        workflowUpstream.setIndex(upstream);
        upstreams.add(workflowUpstream);

        if (logger.isDebugEnabled()) {
            logger.debug("add upstream: {}", upstream);
        }
    }

    public void addUpstream(
            int upstream, List<WorkFlowUpstream.OutputInputMapEntry> outputInputMapEntries) {
        WorkFlowUpstream workflowUpstream = new WorkFlowUpstream();
        workflowUpstream.setIndex(upstream);

        outputInputMapEntries.forEach(
                outputInputMapEntry -> {
                    //                    int output = outputInputMapEntry.getOutput();
                    //                    int input = outputInputMapEntry.getInput();

                    workflowUpstream.getOutputInputMap().add(outputInputMapEntry);
                });

        upstreams.add(workflowUpstream);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "add upstream: {}, outputInputMapEntries: {}", upstream, outputInputMapEntries);
        }
    }
}
