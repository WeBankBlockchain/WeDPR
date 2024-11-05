package com.webank.wedpr.components.scheduler.python;

import com.webank.wedpr.common.utils.WeDPRException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PythonScriptExecutor.class);

    private static final int SCRIPT_SUCCESS = 0;

    private final String pythonInterpreter;
    private final Map<String, String> environmentVariables = new HashMap<>();

    public PythonScriptExecutor() {
        this("python3", null);
    }

    public PythonScriptExecutor(
            String pythonInterpreter, Map<String, String> environmentVariables) {
        this.pythonInterpreter = pythonInterpreter;
        this.environmentVariables.putAll(System.getenv());

        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            this.environmentVariables.putAll(environmentVariables);
        }
    }

    public void addEnvironmentVariable(String key, String value) {
        this.environmentVariables.put(key, value);
    }

    public String executeScript(String scriptPath, List<String> scriptArgs)
            throws IOException, InterruptedException, WeDPRException {

        // params
        List<String> params = new ArrayList<>();
        params.add(pythonInterpreter);
        params.add(scriptPath);
        if (scriptArgs != null && !scriptArgs.isEmpty()) {
            params.addAll(scriptArgs);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(params.toArray(new String[0]));
        processBuilder.redirectErrorStream(true);
        if (!environmentVariables.isEmpty()) {
            processBuilder.environment().putAll(environmentVariables);
        }

        Process process = processBuilder.start();

        StringBuilder outputStringBuilder = new StringBuilder();

        try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader); ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                outputStringBuilder.append(line).append("\n");
            }
        }

        // wait for python script finish
        int exitCode = process.waitFor();
        if (exitCode != SCRIPT_SUCCESS) {
            logger.warn(
                    "the python script execute failed, exit code: {}, output: {}",
                    exitCode,
                    outputStringBuilder);
            throw new WeDPRException(
                    "the python script execute failed, python script: "
                            + scriptPath
                            + " ,output: "
                            + outputStringBuilder);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("the python script execute successfully, output: {}", outputStringBuilder);
        }

        return outputStringBuilder.toString();
    }
}
