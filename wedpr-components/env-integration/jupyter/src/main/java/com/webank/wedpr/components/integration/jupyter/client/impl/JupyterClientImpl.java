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
package com.webank.wedpr.components.integration.jupyter.client.impl;

import com.webank.wedpr.common.config.WeDPRCommonConfig;
import com.webank.wedpr.common.protocol.task.ShellParameters;
import com.webank.wedpr.common.protocol.task.TaskExecutionContext;
import com.webank.wedpr.common.protocol.task.TaskResponse;
import com.webank.wedpr.common.protocol.task.TaskType;
import com.webank.wedpr.common.utils.*;
import com.webank.wedpr.components.http.client.HttpClientImpl;
import com.webank.wedpr.components.integration.jupyter.client.JupyterClient;
import com.webank.wedpr.components.integration.jupyter.core.JupyterConfig;
import com.webank.wedpr.components.integration.jupyter.dao.JupyterInfoDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
import com.webank.wedpr.components.task.plugin.api.model.CommandTaskExecutionContext;
import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JupyterClientImpl implements JupyterClient {
    private static final Logger logger = LoggerFactory.getLogger(JupyterClientImpl.class);
    private final SysConfigMapper sysConfigMapper;
    private final BaseResponseFactory responseFactory = new WeDPRResponseFactory();

    public JupyterClientImpl(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    protected String getCodeTemplate(String key, boolean mustExist) throws WeDPRException {
        SysConfigDO result = this.sysConfigMapper.queryConfig(key);
        if (result != null) {
            return result.getConfigValue();
        }
        if (mustExist) {
            throw new WeDPRException(
                    "The code template for " + key + " not configure! Please configure firstly");
        }
        return null;
    }

    protected HttpClientImpl generateHttpClient(String url) {
        String apiUri =
                String.format(
                        "%s/%s/%s",
                        url,
                        WeDPRCommonConfig.getWedprWorkerApiPath(),
                        WeDPRCommonConfig.getWedprWorkerSubmitTaskMethod());
        return new HttpClientImpl(
                Common.getUrl(apiUri),
                JupyterConfig.getMaxTotalConnection(),
                JupyterConfig.buildConfig(),
                responseFactory);
    }

    protected WeDPRResponse submitTask(String method, JupyterInfoDO jupyterInfoDO, String code)
            throws Exception {
        logger.info("Submit code to: {}", jupyterInfoDO.getAccessEntry());
        HttpClientImpl httpClient = generateHttpClient(jupyterInfoDO.getAccessEntry());
        TaskExecutionContext taskRequest = new CommandTaskExecutionContext();
        taskRequest.setTaskID(WeDPRUuidGenerator.generateID());
        taskRequest.setTaskType(TaskType.SHELL.getType());
        ShellParameters shellParameters = new ShellParameters(code);
        taskRequest.setTaskParameters(shellParameters.serialize());
        taskRequest.setParameterMap(JupyterClient.generateParamMap(jupyterInfoDO));
        WeDPRResponse response = (WeDPRResponse) httpClient.executePost(taskRequest);
        if (response != null && response.statusOk()) {
            // Note: here can't convert Object to TaskResponse directly
            TaskResponse taskResponse =
                    ObjectMapperFactory.getObjectMapper()
                            .convertValue(response.getData(), TaskResponse.class);
            if (taskResponse != null && taskResponse.statusOk()) {
                logger.info(
                        "submitTask for method {} success, taskID: {}, jupyterInfo: {}, code: {}, response: {}",
                        method,
                        taskRequest.getTaskID(),
                        jupyterInfoDO.toString(),
                        code,
                        response);
                return response;
            }
        }
        logger.error(
                "submitTask for method {} failed, taskID: {}, jupyterInfo: {}, code: {}, response: {}",
                method,
                taskRequest.getTaskID(),
                jupyterInfoDO.toString(),
                code,
                (response == null ? "null" : response.toString()));
        throw new WeDPRException(
                "submitTask for "
                        + method
                        + " failed, taskID: "
                        + taskRequest.getTaskID()
                        + ", jupyterInfo: "
                        + jupyterInfoDO.toString()
                        + ", response: "
                        + (response == null ? "null" : response.toString()));
    }

    @Override
    public WeDPRResponse create(JupyterInfoDO jupyterInfo) throws Exception {
        // create user and start jupyter
        String code =
                getCodeTemplate(WeDPRCommonConfig.getCodeTemplateKeyCreateUser(), true)
                        + WeDPRCommonConfig.getShellCodeConnector()
                        + getCodeTemplate(JupyterConfig.getCodeTemplateKeyStartJupyter(), true);
        logger.info("create jupyter, info: {}, code: {}", jupyterInfo.toString(), code);
        return submitTask("createJupyter", jupyterInfo, code);
    }

    @Override
    public WeDPRResponse start(JupyterInfoDO jupyterInfo) throws Exception {
        // Note: should check the existence firstly
        List<String> commands = new ArrayList<>();
        commands.add(ShellConstant.BASH_HEADER);
        commands.add(ShellConstant.BASE_DIR_CMD);
        // check the pid existence
        String getJupyterPidCode =
                getCodeTemplate(JupyterConfig.getCodeTemplateKeyGetJupyterPid(), true);
        commands.add(String.format("jupyter_pid=$(%s)", getJupyterPidCode));
        commands.add("if [ ! -z ${jupyter_pid} ];then ");
        commands.add("echo \"${jupyter_pid}\"");
        commands.add("else");
        commands.add(getCodeTemplate(JupyterConfig.getCodeTemplateKeyStartJupyter(), true));
        commands.add("sleep 1.5");
        commands.add(String.format("echo $(%s)", getJupyterPidCode));
        commands.add("fi");
        String code = commands.stream().collect(Collectors.joining(System.lineSeparator()));
        logger.info("start jupyter, info: {}, code: {}", jupyterInfo.toString(), code);
        return submitTask("startJupyter", jupyterInfo, code);
    }

    @Override
    public WeDPRResponse stop(JupyterInfoDO jupyterInfo) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add(ShellConstant.BASH_HEADER);
        commands.add(ShellConstant.BASE_DIR_CMD);
        // check the pid existence
        String getJupyterPidCode =
                getCodeTemplate(JupyterConfig.getCodeTemplateKeyGetJupyterPid(), true);
        commands.add(String.format("jupyter_pid=$(%s)", getJupyterPidCode));
        commands.add("[ ! -z ${jupyter_pid} ] && kill ${jupyter_pid} > /dev/null");
        commands.add("[ -z ${jupyter_pid} ] && exit 0");
        commands.add("sleep 2");
        commands.add(String.format("jupyter_pid=$(%s)", getJupyterPidCode));
        commands.add("[ ! -z ${jupyter_pid} ] && exit 1");
        commands.add("[ -z ${jupyter_pid} ] && exit 0");
        String code = commands.stream().collect(Collectors.joining(System.lineSeparator()));
        logger.info("stop jupyter, info: {}, code: {}", jupyterInfo.toString(), code);
        return submitTask("stopJupyter", jupyterInfo, code);
    }
}
