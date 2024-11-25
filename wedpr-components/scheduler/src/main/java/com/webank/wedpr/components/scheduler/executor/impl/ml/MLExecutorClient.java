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

package com.webank.wedpr.components.scheduler.executor.impl.ml;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.ServiceName;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.loadbalancer.LoadBalancer;
import com.webank.wedpr.components.scheduler.executor.impl.ml.model.ModelJobResult;
import com.webank.wedpr.components.scheduler.executor.impl.ml.request.GetTaskResultRequest;
import com.webank.wedpr.sdk.jni.transport.model.ServiceMeta;

public class MLExecutorClient {
    public static Object getJobResult(LoadBalancer loadBalancer, GetTaskResultRequest request)
            throws Exception {

        ServiceMeta.EntryPointMeta entryPoint =
                loadBalancer.selectService(
                        LoadBalancer.Policy.ROUND_ROBIN,
                        ServiceName.MODEL.getValue(),
                        WeDPRCommonConfig.getWedprZone(),
                        null);
        if (entryPoint == null) {
            throw new WeDPRException(
                    "Cannot find client entrypoint for service: "
                            + ServiceName.MODEL.getValue()
                            + ", zone: "
                            + WeDPRCommonConfig.getWedprZone());
        }
        String url = entryPoint.getUrl(null);
        HttpClientImpl httpClient =
                new HttpClientImpl(
                        MLExecutorConfig.getObtainJobResultApiUrl(url, request.getJobID()),
                        MLExecutorConfig.getMaxTotalConnection(),
                        MLExecutorConfig.buildConfig(),
                        null);
        String content = httpClient.executePostAndGetString(request, Constant.HTTP_SUCCESS);
        ModelJobResult modelJobResult = ModelJobResult.deserialize(content);
        if (modelJobResult == null) {
            return null;
        }
        if (modelJobResult.getData() == null) {
            return null;
        }
        if (modelJobResult.getData().getJobPlanetResult() == null) {
            return null;
        }
        return modelJobResult.getData();
    }
}
