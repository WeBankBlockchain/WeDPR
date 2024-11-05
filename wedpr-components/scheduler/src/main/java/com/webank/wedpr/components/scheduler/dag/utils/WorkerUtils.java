package com.webank.wedpr.components.scheduler.dag.utils;

import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.worker.WorkerStatus;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;
import lombok.SneakyThrows;

public class WorkerUtils {

    private WorkerUtils() {}

    public static String toWorkId(String jobId, int index, String workType) {
        return jobId + "_" + index + "_" + workType;
    }

    public static void sleep(int sleepMilli) {
        if (sleepMilli > 0) {
            try {
                Thread.sleep(sleepMilli);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @SneakyThrows
    public static JobWorker toJobWorker(String jobId, WorkFlowNode workFlowNode) {
        JobWorker jobWorker = new JobWorker();
        jobWorker.setJobId(jobId);
        jobWorker.setWorkerId(
                WorkerUtils.toWorkId(jobId, workFlowNode.getIndex(), workFlowNode.getType()));
        jobWorker.setType(workFlowNode.getType());
        jobWorker.setArgs(
                ObjectMapperFactory.getObjectMapper().writeValueAsString(workFlowNode.getArgs()));
        jobWorker.setStatus(WorkerStatus.PENDING.name());
        jobWorker.setUpstreams(
                ObjectMapperFactory.getObjectMapper()
                        .writeValueAsString(workFlowNode.getUpstreams()));

        return jobWorker;
    }
}
