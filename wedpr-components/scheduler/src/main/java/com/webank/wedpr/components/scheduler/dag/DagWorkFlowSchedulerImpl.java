package com.webank.wedpr.components.scheduler.dag;

// import static com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils.toJobWorker;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.dag.api.WorkFlowScheduler;
import com.webank.wedpr.components.scheduler.dag.base.DAG;
import com.webank.wedpr.components.scheduler.dag.base.DAGNode;
import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import com.webank.wedpr.components.scheduler.dag.utils.WorkerUtils;
import com.webank.wedpr.components.scheduler.dag.worker.*;
import com.webank.wedpr.components.scheduler.mapper.JobWorkerMapper;
import com.webank.wedpr.components.scheduler.workflow.WorkFlow;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowNode;
import com.webank.wedpr.components.scheduler.workflow.WorkFlowUpstream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DagWorkFlowSchedulerImpl implements WorkFlowScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DagWorkFlowSchedulerImpl.class);

    private LoadBalancer loadBalancer;
    private JobWorkerMapper jobWorkerMapper;

    // TODO: make it config items
    private final Integer workerRetryTimes = -1;
    private final Integer workerRetryDelayMillis = -1;

    public DagWorkFlowSchedulerImpl(LoadBalancer loadBalancer, JobWorkerMapper jobWorkerMapper) {
        this.loadBalancer = loadBalancer;
        this.jobWorkerMapper = jobWorkerMapper;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public JobWorkerMapper getJobWorkerMapper() {
        return jobWorkerMapper;
    }

    public void setJobWorkerMapper(JobWorkerMapper jobWorkerMapper) {
        this.jobWorkerMapper = jobWorkerMapper;
    }

    public void saveOrUpdateJobWorker(JobWorker jobWorker) {

        int i = jobWorkerMapper.insertJobWorker(jobWorker);
        if (i == 1) {
            // insert ok
            logger.info(
                    "## save job worker successfully, jobId: {}, workerId: {}, jobWorker: {}",
                    jobWorker.getJobId(),
                    jobWorker.getWorkerId(),
                    jobWorker);
        } else {
            // worker already exist, update worker status
            JobWorker oldJobWorker = jobWorkerMapper.selectJobWorkerById(jobWorker.getWorkerId());
            logger.info(
                    "## the job worker has been already exist, workerId: {}, workerStatus: {}",
                    oldJobWorker.getWorkerId(),
                    oldJobWorker.getStatus());
            jobWorker.setStatus(oldJobWorker.getStatus());
        }
    }

    @Override
    public void schedule(String jobId, WorkFlow workFlow) throws WeDPRException {

        // prepare for dag
        List<Worker> workerList = prepareDag(jobId, workFlow);

        // construct dag
        DAG<Integer> dag = createDag(jobId, workerList);

        // execute dag
        executeDag(jobId, workerList, dag);
    }

    public List<Worker> prepareDag(String jobId, WorkFlow workFlow) throws WeDPRException {

        List<Worker> workerList = new ArrayList<>();

        for (WorkFlowNode workFlowNode : workFlow.getWorkflow()) {
            // to job worker
            JobWorker jobWorker = WorkerUtils.toJobWorker(jobId, workFlowNode);

            if (logger.isDebugEnabled()) {
                logger.debug("## new jobWorker: {}", jobWorker);
            }

            // save job worker
            saveOrUpdateJobWorker(jobWorker);

            // build worker
            Worker worker =
                    WorkerFactory.buildWorker(
                            jobWorker,
                            workerRetryTimes,
                            workerRetryDelayMillis,
                            loadBalancer,
                            jobWorkerMapper);
            workerList.add(worker);
        }

        return workerList;
    }

    @SneakyThrows
    public DAG<Integer> createDag(String jobId, List<Worker> workerList) {
        // create a directed graph
        DAG<Integer> dag = new DAG<>();

        // add the vertices
        for (int i = 0; i < workerList.size(); i++) {
            dag.createNode(i);
        }

        int index = 0;
        // add edges to create a circuit
        for (Worker worker : workerList) {
            List<WorkFlowUpstream> upstreams = worker.getJobWorker().toUpstreams();
            if (!upstreams.isEmpty()) {
                for (WorkFlowUpstream upstream : upstreams) {
                    dag.addEdge(upstream.getIndex(), index);
                }
            }

            index++;
        }

        dag.update();

        logger.info("create dag graph end, jobId: {}, dag: {}", jobId, dag);
        return dag;
    }

    public void executeDag(String jobId, List<Worker> taskWorkers, DAG<Integer> dag) {
        List<Integer> execOrderList = new ArrayList<>();

        try {
            dag.visit(
                    new Consumer<DAGNode<Integer>>() {
                        @SneakyThrows
                        @Override
                        public void accept(DAGNode<Integer> dagNode) {
                            Integer index = dagNode.getObject();
                            execOrderList.add(index);
                            executeWorker(taskWorkers.get(index));
                        }
                    });

            logger.info(
                    "dag executed successfully, jobId: {}, execOrder: {}", jobId, execOrderList);
        } catch (Exception e) {
            logger.error("dag executed failed, jobId: {}, execOrder: {}", jobId, execOrderList);
            throw e;
        }
    }

    /**
     * execute single worker
     *
     * @param worker
     * @throws WeDPRException
     */
    public void executeWorker(Worker worker) throws Exception {
        JobWorker jobWorker = worker.getJobWorker();

        String jobId = jobWorker.getJobId();
        String workerId = jobWorker.getWorkerId();
        String workerStatus = jobWorker.getStatus();

        try {
            if (workerStatus.equals(WorkerStatus.SUCCESS.name())) {
                logger.info(
                        "worker has been executed successfully, jobId: {}, workId: {}",
                        jobId,
                        workerId);
                return;
            }
            WorkerStatus status = worker.run(jobWorker.getStatus());
            if (status != WorkerStatus.KILLED) {
                jobWorkerMapper.updateJobWorkerStatus(workerId, status.name());
                logger.info("worker executed successfully, jobId: {}, workId: {}", jobId, workerId);
            } else {
                logger.info("worker has been killed, jobId: {}, workId: {}", jobId, workerId);
            }
        } catch (Exception e) {
            logger.error("worker executed failed, jobId: {}, workId: {}, e: ", jobId, workerId, e);
            jobWorkerMapper.updateJobWorkerStatus(workerId, WorkerStatus.FAILURE.name());
            throw e;
        }
    }
}
