package com.webank.wedpr.components.dataset.utils;

import com.alibaba.druid.util.HexBin;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {}

    public static int getFileLinesNumber(String filePath) throws DatasetException {
        try (FileReader fileReader = new FileReader(filePath);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader)) {
            lineNumberReader.skip(Long.MAX_VALUE);
            return lineNumberReader.getLineNumber();
        } catch (Exception e) {
            logger.error("get file lines number exception, filePath: {}, e: ", filePath, e);
            throw new DatasetException(
                    "get file lines number exception, filePath: "
                            + filePath
                            + " ,e: "
                            + e.getMessage());
        }
    }

    /**
     * remove directory
     *
     * @param directory
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            // Retrieve all files and subdirectories in the directory
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Recursively delete each file and subdirectory
                    deleteDirectory(file);
                }
            }
        }

        Files.delete(directory.toPath());
    }

    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.length();
    }

    public static String calculateFileHash(String filePath, String algorithm)
            throws DatasetException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unsupported hash algorithm type, algorithm: {}", algorithm);
            throw new DatasetException("Unsupported hash algorithm type, algorithm: " + algorithm);
        }

        byte[] buffer = new byte[1024 * 10];
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        } catch (Exception e) {
            logger.error(
                    "Exception in calculating file hash, algorithm: {}, file: {}, e: ",
                    algorithm,
                    filePath,
                    e);
            throw new DatasetException("Exception in calculating file hash, e: " + e.getMessage());
        }

        return HexBin.encode(digest.digest(), false);
    }
}
