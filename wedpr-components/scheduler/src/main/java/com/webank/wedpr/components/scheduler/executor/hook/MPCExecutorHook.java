package com.webank.wedpr.components.scheduler.executor.hook;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.request.MPCJobRequest;

public class MPCExecutorHook implements ExecutorHook {
    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        // get the jobParam
        MPCJobParam jobParam = (MPCJobParam) jobDO.getJobParam();

        MPCJobRequest mpcJobRequest = (MPCJobRequest) jobParam.toMPCJobRequest();

        mpcJobRequest.setTaskID(jobDO.getTaskID());
        return mpcJobRequest;
    }
}
