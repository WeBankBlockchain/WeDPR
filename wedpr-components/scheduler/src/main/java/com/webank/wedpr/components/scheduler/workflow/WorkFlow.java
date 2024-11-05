package com.webank.wedpr.components.scheduler.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
@Getter
public class WorkFlow {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlow.class);

    public WorkFlow() {}

    public WorkFlow(String jobId) {
        this.jobId = jobId;
    }

    @JsonIgnore private int index = 0;

    private String jobId;
    private String agency;
    private List<WorkFlowNode> workflow = new ArrayList<>();

    @JsonIgnore
    public int getNextIndex() {
        return index++;
    }

    public WorkFlowNode addWorkFlowNode(List<Integer> upstreams, String type, Object args) {
        return addWorkFlowNode(getNextIndex(), upstreams, type, args);
    }

    private WorkFlowNode addWorkFlowNode(
            int index, List<Integer> upstreams, String type, Object args) {

        WorkFlowNode workflowNode = new WorkFlowNode();
        workflowNode.setIndex(index);
        workflowNode.setType(type);
        workflowNode.setArgs(args);

        if (upstreams != null && !upstreams.isEmpty()) {
            upstreams.forEach(workflowNode::addUpstream);
        }

        workflow.add(workflowNode);
        logger.debug("add workflow jobId: {}, node: {}", jobId, workflowNode);

        return workflowNode;
    }
}
