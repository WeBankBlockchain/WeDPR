package com.webank.wedpr.components.dataset.utils;

import com.webank.wedpr.components.dataset.exception.DatasetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtils {
    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);

    public static void isValidTimestamp(String timestamp) throws DatasetException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
            formatter.parse(timestamp);
        } catch (DateTimeParseException e) {
            logger.error("illegal timestamp string, timestamp: {}", timestamp);
            throw new DatasetException("illegal timestamp string, value: " + timestamp);
        }
    }

    public static void isValidDateFormat(String timestamp) throws DatasetException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
            formatter.parse(timestamp);
        } catch (DateTimeParseException e) {
            logger.error("illegal date string, timestamp: {}", timestamp);
            throw new DatasetException("illegal date string, value: " + timestamp);
        }
    }

    public static boolean isDateExpired(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        LocalDate currentDate = LocalDate.now();
        return date.isBefore(currentDate);
    }
}
