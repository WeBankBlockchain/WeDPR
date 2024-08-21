/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.scheduler.demo;

import com.webank.wedpr.components.project.dao.JobDO;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobParam;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.XGBModelSetting;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMetaBuilder;
import com.webank.wedpr.components.storage.builder.StoragePathBuilder;
import com.webank.wedpr.components.storage.config.HdfsStorageConfig;
import com.webank.wedpr.components.storage.config.LocalStorageConfig;
import com.webank.wedpr.core.protocol.StorageType;
import com.webank.wedpr.core.utils.ObjectMapperFactory;
import java.util.ArrayList;
import java.util.List;

public class GenerateJobParam {
    public static void main(String[] args) throws Exception {
        /*System.out.println("======= GenerateJobParam for PSI ========== ");
        PSIJobParam psiJobParam = new PSIJobParam();
        psiJobParam.setSyncResult(true);
        List<PSIJobParam.PartyResourceInfo> partyResourceInfos = new ArrayList<>();
        PSIJobParam.PartyResourceInfo partyResourceInfo = new PSIJobParam.PartyResourceInfo();
        partyResourceInfo.setFieldList(Collections.singletonList("id"));
        FileMetaBuilder fileMetaBuilder = createFileMetaBuilder();

        partyResourceInfo.setInput(
                fileMetaBuilder.build(
                        StorageType.HDFS,
                        "/user/ppc/webank/ppc/d-9176460455323653",
                        "yujiechen",
                        "WeBank"));
        partyResourceInfo.setOutput(
                fileMetaBuilder.build(StorageType.HDFS, "yujiechen/psi", "yujiechen", "WeBank"));
        partyResourceInfos.add(partyResourceInfo);

        partyResourceInfo = new PSIJobParam.PartyResourceInfo();
        partyResourceInfo.setFieldList(Collections.singletonList("id"));
        partyResourceInfo.setInput(
                fileMetaBuilder.build(
                        StorageType.HDFS,
                        "/user/ppc/webank/ppc/d-9176454376531973",
                        "yujiechen",
                        "WeBank"));
        partyResourceInfo.setOutput(
                fileMetaBuilder.build(StorageType.HDFS, "yujiechen/psi", "yujiechen", "WeBank"));
        partyResourceInfos.add(partyResourceInfo);
        psiJobParam.setPartyResourceInfoList(partyResourceInfos);
        JobDO jobDO = new JobDO();
        jobDO.setParam(psiJobParam.serialize());
        System.out.println(ObjectMapperFactory.getObjectMapper().writeValueAsString(jobDO));
        System.out.println("==== GenerateJobParam for PSI Finished ======= ");*/
        ModelJobParam modelJobParam = new ModelJobParam();
        List<DatasetInfo> dataSetList = new ArrayList<>();
        DatasetInfo datasetInfo = new DatasetInfo();
        FileMetaBuilder fileMetaBuilder = createFileMetaBuilder();
        datasetInfo.setDataset(
                fileMetaBuilder.build(
                        StorageType.HDFS,
                        "/user/ppc/webank/ppc/d-9187101688539141",
                        "yujiechen",
                        "webank"));
        datasetInfo.setLabelProvider(false);
        dataSetList.add(datasetInfo);

        DatasetInfo sgdDataset = new DatasetInfo();
        sgdDataset.setDataset(
                fileMetaBuilder.build(
                        StorageType.HDFS,
                        "/user/ppc/sgd/ppc/d-9187103499692037",
                        "yujiechen",
                        "webank"));
        sgdDataset.setLabelProvider(true);
        dataSetList.add(sgdDataset);

        modelJobParam.setDataSetList(dataSetList);
        modelJobParam.setModelSetting(new XGBModelSetting());
        JobDO jobDO = new JobDO();
        jobDO.setParam(modelJobParam.serialize());
        System.out.println(
                "######## job Param: "
                        + ObjectMapperFactory.getObjectMapper().writeValueAsString(jobDO));
    }

    private static FileMetaBuilder createFileMetaBuilder() {
        HdfsStorageConfig hdfsStorageConfig = new HdfsStorageConfig();
        hdfsStorageConfig.setBaseDir("/user/ppc/webank");
        hdfsStorageConfig.setUser("ppc");
        LocalStorageConfig localStorageConfig = new LocalStorageConfig();
        return new FileMetaBuilder(new StoragePathBuilder(hdfsStorageConfig, localStorageConfig));
    }
}
