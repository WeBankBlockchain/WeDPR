package com.webank.wedpr.components.scheduler.executor.impl.psi;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.scheduler.executor.impl.mpc.MPCJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.psi.model.PSIJobParam;
import com.webank.wedpr.components.storage.api.FileStorageInterface;

public class MPCPSIExecutor extends PSIExecutor {
    public MPCPSIExecutor(FileStorageInterface storage, FileMetaBuilder fileMetaBuilder) {
        super(storage, fileMetaBuilder, null);
    }

    @Override
    public Object prepare(JobDO jobDO) throws Exception {
        // get the jobParam
        PSIJobParam psiJobParam =
                ((MPCJobParam) jobDO.getJobParam())
                        .toPSIJobParam(this.fileMetaBuilder, this.storage);
        psiJobParam.setTaskID(jobDO.getTaskID());
        preparePSIJob(jobDO, psiJobParam);
        return jobDO.getJobRequest();
    }
}
