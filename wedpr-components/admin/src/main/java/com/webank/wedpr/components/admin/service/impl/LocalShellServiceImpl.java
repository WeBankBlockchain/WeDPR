package com.webank.wedpr.components.admin.service.impl;

import com.webank.wedpr.common.protocol.CertScriptCmdEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.config.WedprCertConfig;
import com.webank.wedpr.components.admin.service.LocalShellService;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by caryliao on 2024/8/24 20:17 */
@Service
@Slf4j
public class LocalShellServiceImpl implements LocalShellService {
    @Autowired private WedprCertConfig wedprCertConfig;

    @Override
    public boolean buildAuthorityCsrToCrt(String agencyName, String csrPath, long days) {
        boolean res = false;
        try {
            log.info("buildAuthorityCsrToCrt start");
            String certScriptDir = wedprCertConfig.getCertScriptDir();
            String certScript = wedprCertConfig.getCertScript();
            String rootCertPath = wedprCertConfig.getRootCertPath();
            log.info("buildAuthorityCsrToCrt certScriptDir:{}", certScriptDir);
            log.info("buildAuthorityCsrToCrt certScript:{}", certScript);
            log.info("buildAuthorityCsrToCrt rootCertPath:{}", rootCertPath);
            log.info("buildAuthorityCsrToCrt csrPath:{}", csrPath);
            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            certScript,
                            CertScriptCmdEnum.CSR_TO_CRT.getName(),
                            rootCertPath,
                            csrPath,
                            String.valueOf(days));
            log.info("shell command:{}", processBuilder.command());
            processBuilder.directory(new File(certScriptDir));
            String result = executeScript(processBuilder);
            log.info("buildAuthorityCsrToCrt result:" + result);
            if (!Utils.isEmpty(result) && result.contains(Constant.CERT_SCRIPT_EXECUTE_OK)) {
                res = true;
                log.info("buildAuthorityCsrToCrt execute ok");
            }
        } catch (Exception e) {
            log.error("buildAuthorityCsrToCrt error", e);
            res = false;
        }
        return res;
    }

    public String executeScript(ProcessBuilder processBuilder) throws WeDPRException {
        Process process = null;
        String result = "";
        try {
            process = processBuilder.start();
            log.info("executeScript start");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            BufferedReader bufferedReaderError =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder stringBuilerErrorMsg = new StringBuilder();
            String lineError;
            while ((lineError = bufferedReaderError.readLine()) != null) {
                stringBuilerErrorMsg.append(lineError).append("\n");
            }
            log.info("executeScript error message:{}", stringBuilerErrorMsg);
            result = stringBuilder.toString();
        } catch (Exception e) {
            log.error("executeScript error", e);
            throw new WeDPRException("executeScript error");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }
}
