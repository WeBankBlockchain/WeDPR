package com.webank.wedpr.components.admin.common;

import com.webank.wedpr.components.token.auth.TokenUtils;
import com.webank.wedpr.components.token.auth.model.UserToken;
import com.webank.wedpr.core.protocol.UserRoleEnum;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 计算时间差（以天为单位）
        return ChronoUnit.DAYS.between(currentTime, inputTime);
    }

    public static LocalDateTime getLocalDateTime(String inputTime) {
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 将字符串转换为 LocalDateTime
        LocalDateTime parsedTime = LocalDateTime.parse(inputTime, formatter);
        return parsedTime;
    }

    public static boolean fileToZip(String sourcePath, String fileName) { // NOSONAR
        BufferedInputStream bis = null;
        ZipOutputStream zos = null;
        try { // NOSONAR
            log.info("fileToZip fileName:{} sourcePath:{}", fileName, sourcePath);
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                log.error("No source filePath");
                return false;
            }
            File zipFlie =
                    new File(sourcePath + File.separator + fileName + Constant.ZIP_FILE_SUFFIX);

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
                // zip实体，添加到压缩包
                ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
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
}
