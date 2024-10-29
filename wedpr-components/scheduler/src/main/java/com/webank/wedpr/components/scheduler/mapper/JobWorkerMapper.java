package com.webank.wedpr.components.scheduler.mapper;

import com.webank.wedpr.components.scheduler.dag.entity.JobWorker;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobWorkerMapper {

    /**
     * query jobWorker by workId
     *
     * @param workerId
     * @return
     */
    JobWorker selectJobWorkerById(@Param("workerId") String workerId);

    /**
     * insert jobWorker
     *
     * @param jobWorker
     * @return
     */
    int insertJobWorker(JobWorker jobWorker);

    /**
     * update jobWorker status
     *
     * @param workerId
     * @param status
     * @return
     */
    int updateJobWorkerStatus(@Param("workerId") String workerId, @Param("status") String status);

    /**
     * @param jobWorkers
     * @return
     */
    int batchInsertJobWorkers(List<JobWorker> jobWorkers);
}
