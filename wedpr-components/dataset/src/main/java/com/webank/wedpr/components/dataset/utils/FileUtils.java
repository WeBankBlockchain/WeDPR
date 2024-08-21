package com.webank.wedpr.components.dataset.utils;

import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.core.utils.WeDPRException;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static int getFileLinesNumber(String filePath) throws DatasetException {
        try (FileReader fileReader = new FileReader(filePath);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader)) {
            lineNumberReader.skip(Long.MAX_VALUE);
            return lineNumberReader.getLineNumber();
        } catch (Exception e) {
            logger.error("获取文件行数异常, filePath: {}, e: ", filePath, e);
            throw new DatasetException(
                    "获取文件行数异常, filePath: " + filePath + " ,e: " + e.getMessage());
        }
    }

    /**
     * 删除目录
     *
     * @param directory
     */
    @SneakyThrows(WeDPRException.class)
    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            // 获取目录中的所有文件和子目录
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 递归删除每个文件和子目录
                    deleteDirectory(file);
                }
            }
        }
        // 删除空目录
        if (!directory.delete()) {
            throw new WeDPRException("deleteDirectory for " + directory + "failed!");
        }
    }
}
