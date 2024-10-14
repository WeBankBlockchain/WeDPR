package com.webank.wedpr.components.scheduler.executor.impl.mpc;

import com.webank.wedpr.common.protocol.JobType;
import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.ExecutorParamChecker;
import java.util.Collections;
import java.util.List;

public class MPCExecutorParamChecker implements ExecutorParamChecker {

    @Override
    public List<JobType> getJobTypeList() {
        return Collections.singletonList(JobType.MPC);
    }

    @Override
    public Object checkAndParseJob(JobDO jobDO) throws Exception {
        MPCJobParam modelJobParam = MPCJobParam.deserialize(jobDO.getParam());
        modelJobParam.setJobID(jobDO.getId());
        modelJobParam.setJobType(jobDO.getType());
        modelJobParam.setDatasetIDList(jobDO.getDatasetList());
        // check the param
        modelJobParam.check();

        return modelJobParam;
    }
}
