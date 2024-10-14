package com.webank.wedpr.components.admin.common;

import com.webank.wedpr.common.protocol.CertStatusEnum;
import com.webank.wedpr.common.protocol.CertStatusViewEnum;
import com.webank.wedpr.common.protocol.UserRoleEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import java.io.*;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/** Created by caryliao on 2024/8/22 22:29 */
@Slf4j
public class Utils {
    public static final String CERT_SCRIPT_NAME = "cert_tool.zip";

    public static UserToken checkPermission(HttpServletRequest request) throws WeDPRException {
        UserToken userToken = TokenUtils.getLoginUser(request);
        String username = userToken.getUsername();
        if (!UserRoleEnum.AGENCY_ADMIN.getRoleName().equals(userToken.getRoleName())) {
            log.info("用户名：{}， 角色：{}", username, userToken.getRoleName());
            throw new WeDPRException("无权限访问该接口");
        }
        return userToken;
    }

    public static boolean isSafeCommand(String command) {
        if (StringUtils.isEmpty(command)) return true;
        command = command.replace("\n", "\\n");
        List<String> blackList = Arrays.asList(new String[] {";", "\\n", "&", "|", "$", "`", ".."});
        for (String str : blackList) {
            if (command.contains(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断对象是否Empty(null或元素为0)<br>
     * 实用于对如下对象做判断:String Collection及其子类 Map及其子类
     *
     * @param object 待检查对象
     * @return boolean 返回的布尔值
     */
    public static boolean isEmpty(Object object) {
        if (object == null) return true;
        if (object == "") return true;
        if (object instanceof String) {
            if (((String) object).length() == 0) {
                return true;
            }
        } else if (object instanceof Collection) {
            if (((Collection) object).size() == 0) {
                return true;
            }
        } else if (object instanceof Map) {
            if (((Map) object).size() == 0) {
                return true;
            }
        }
        return false;
    }

    public static long getDaysDifference(LocalDateTime inputTime) {
        LocalDateTime currentTime = LocalDateTime.now();
        long secondsDifference = ChronoUnit.SECONDS.between(currentTime, inputTime);
        double oneDaySeconds = 24 * 3600.0;
        double daysDifference = secondsDifference / oneDaySeconds;
        log.info("cert daysDifference:{}", daysDifference);
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setRoundingMode(RoundingMode.CEILING);
        Integer daysDifferenceInt = Integer.parseInt(decimalFormat.format(daysDifference));
        log.info("cert daysDifferenceInt:{}", daysDifferenceInt);
        return daysDifferenceInt;
    }

    public static LocalDateTime getLocalDateTime(String inputTime) {
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 将字符串转换为 LocalDateTime
        LocalDateTime parsedTime = LocalDateTime.parse(inputTime, formatter);
        return parsedTime;
    }

    public static boolean fileToZip(String sourcePath, String agencyName) { // NOSONAR
        BufferedInputStream bis = null;
        ZipOutputStream zos = null;
        try { // NOSONAR
            log.info("fileToZip agencyName:{} sourcePath:{}", agencyName, sourcePath);
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                log.error("No source filePath");
                return false;
            }
            String zipFileName = agencyName + Constant.ZIP_FILE_SUFFIX;
            File zipFlie = new File(sourcePath + File.separator + zipFileName);

            File[] sourceFiles = sourceFile.listFiles();
            if (sourceFiles == null || sourceFiles.length < 1) {
                log.error("No source file");
                return false;
            }
            FileInputStream fis = null;
            FileOutputStream fos = new FileOutputStream(zipFlie); // NOSONAR
            zos = new ZipOutputStream(new BufferedOutputStream(fos)); // NOSONAR
            byte[] bufs = new byte[1024 * 10];
            for (int i = 0; i < sourceFiles.length; i++) {
                String fileName = sourceFiles[i].getName();
                log.info("compress fileName:{}", fileName);
                if (zipFileName.equals(fileName)) {
                    log.info("zip fileName:{} is skip", fileName);
                    continue;
                }
                // zip实体，添加到压缩包
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                // 读取代压缩文件写到压缩包里
                fis = new FileInputStream(sourceFiles[i]); // NOSONAR
                bis = new BufferedInputStream(fis, 1024 * 10); // NOSONAR
                int read = 0;
                while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                    zos.write(bufs, 0, read);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("file to zip error", e);
            return false;
        } finally {
            // 关闭流
            try {
                if (null != bis) bis.close();
                if (null != zos) zos.close();
            } catch (Exception e) {
                log.error("close stream err", e);
            }
        }
    }

    public static String fileToBase64(String path) {
        String base64 = null;
        try {
            log.info("fileToBase64 path:{}", path);
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("fileToBase64 err" + e);
        }
        return base64;
    }

    public static Integer getCertStatusView(Integer certStatus, LocalDateTime expireTime) {
        LocalDateTime now = LocalDateTime.now();
        if (CertStatusEnum.FORBID_CERT.getStatusValue() == certStatus) {
            return CertStatusViewEnum.FORBID_CERT.getStatusValue();
        } else {
            if (expireTime.isAfter(now)) {
                return CertStatusViewEnum.VALID_CERT.getStatusValue();
            } else {
                return CertStatusViewEnum.EXPIRED_CERT.getStatusValue();
            }
        }
    }

    public static String getPercentage(int numerator, int denominator, int decimalPlaces) {
        double result = 0;
        if (denominator != 0) {
            result = (double) numerator / denominator;
        }
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance();
        decimalFormat.setMinimumFractionDigits(decimalPlaces);
        decimalFormat.setMaximumFractionDigits(decimalPlaces);
        decimalFormat.setRoundingMode(java.math.RoundingMode.HALF_UP);
        int percentRate = 100;
        return decimalFormat.format(result * percentRate);
    }

    public static byte[] readInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }
}
