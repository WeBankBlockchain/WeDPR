package com.webank.wedpr.components.scheduler.executor.impl.mpc.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;
import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.scheduler.executor.impl.ExecutorConfig;
import com.webank.wedpr.components.scheduler.executor.impl.model.DatasetInfo;
import com.webank.wedpr.components.scheduler.executor.impl.model.FileMeta;
import com.webank.wedpr.components.scheduler.python.PythonScriptExecutor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MpcUtils {

    private static final Logger logger = LoggerFactory.getLogger(MpcUtils.class);

    private MpcUtils() {}

    public static final String MPC_FIELD_ID_NAME = "id";
    public static final int DEFAULT_MPC_CONTENT_SHARE_BYTES_LENGTH = 64;
    public static final String MPC_FIELD_NORMALIZED_NAMES = "field%d";
    public static final String MPC_SOURCE_RECORD_COUNT_LABEL = "$(source%d_record_count)";
    public static final String MPC_SHARE_BYTES_LENGTH_LABEL = "# BIT_LENGTH = ";

    public static String transSql2MpcCode(String sql)
            throws WeDPRException, IOException, InterruptedException {

        String pythonScript = ExecutorConfig.getMpcCodeGeneratorScriptPath();
        String mpcCodeGeneratorEnvPath = ExecutorConfig.getMpcCodeGeneratorEnvPath();

        return transSql2MpcCode(pythonScript, mpcCodeGeneratorEnvPath, sql);
    }

    public static String transSql2MpcCode(String pythonScript, String env, String sql)
            throws WeDPRException, IOException, InterruptedException {

        PythonScriptExecutor pythonScriptExecutor = new PythonScriptExecutor();

        if (env != null && !env.isEmpty()) {
            pythonScriptExecutor.addEnvironmentVariable("PYTHONPATH", env);
        }

        return pythonScriptExecutor.executeScript(pythonScript, Collections.singletonList(sql));
    }

    public static int getShareBytesLength(String mpcContent) {
        String target = MPC_SHARE_BYTES_LENGTH_LABEL;
        int startIndex = mpcContent.indexOf(target);
        if (startIndex != -1) {
            int endIndex = mpcContent.indexOf('\n', startIndex + target.length());
            if (endIndex != -1) {
                String bitLengthStr =
                        mpcContent.substring(startIndex + target.length(), endIndex).trim();
                try {
                    return Integer.parseInt(bitLengthStr);
                } catch (NumberFormatException e) {
                    return DEFAULT_MPC_CONTENT_SHARE_BYTES_LENGTH;
                }
            }
        }
        return DEFAULT_MPC_CONTENT_SHARE_BYTES_LENGTH;
    }

    public static int getDatasetColumnCount(String mpcContent, int selfIndex)
            throws WeDPRException {
        String[] lines = mpcContent.split("\n");
        // String pattern = "source" + selfIndex + "_column_count\\s*=\\s*";
        Pattern pattern = Pattern.compile("source" + selfIndex + "_column_count\\s*=\\s*");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int index = line.indexOf('=');
                if (index != -1) {
                    String columnCountStr = line.substring(index + 1).trim().replace("\n", "");

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "mpc content dataset column count: {}, index: {}",
                                columnCountStr,
                                selfIndex);
                    }

                    try {
                        return Integer.parseInt(columnCountStr);
                    } catch (NumberFormatException e) {
                        logger.warn(
                                "invalid mpc content, line: {}, index: {}, NumberFormatException: ",
                                columnCountStr,
                                selfIndex,
                                e);
                        throw new WeDPRException(
                                "Not found valid dataset column count in mpc content");
                    }
                }
            }
        }

        throw new WeDPRException("Not found dataset column count in mpc content");
    }

    public static int getIdFieldIndex(List<String> fieldNames) throws WeDPRException {

        int index = 0;
        for (String fieldName : fieldNames) {
            if (fieldName.equals(MPC_FIELD_ID_NAME)) {
                return index;
            }
            index++;
        }

        logger.warn("the id field does not exist, fields: {}", fieldNames);
        throw new WeDPRException("the id field does not exist, fields: " + fieldNames);
    }

    public static List<String> makeFieldNamesNormalized(List<String> fieldNames) {

        List<String> normalizedNames = new ArrayList<>();
        if (fieldNames.contains(MPC_FIELD_ID_NAME)) {
            normalizedNames.add(MPC_FIELD_ID_NAME);
            int size = fieldNames.size();
            for (int i = 0; i < size - 1; i++) {
                normalizedNames.add(String.format(MPC_FIELD_NORMALIZED_NAMES, i));
            }
        } else {
            int size = fieldNames.size();
            for (int i = 0; i < size; i++) {
                normalizedNames.add(String.format(MPC_FIELD_NORMALIZED_NAMES, i));
            }
        }

        return normalizedNames;
    }

    public static List<String> makeSelectFields(int datasetCountNumber) {
        // select columns
        List<String> selectColumns = new ArrayList<>();
        for (int i = 0; i < datasetCountNumber; i++) {
            selectColumns.add(String.format(MPC_FIELD_NORMALIZED_NAMES, i));
        }
        return selectColumns;
    }

    public static List<Integer> makeSelectFieldsIndex(
            List<String> selectFieldNames, List<String> originalFieldNames) throws WeDPRException {
        List<Integer> indexList = new ArrayList<>();
        for (String selectFieldName : selectFieldNames) {
            int index = originalFieldNames.indexOf(selectFieldName);
            if (index == -1) {
                logger.warn("Not found select field index, fieldName: {}", selectFieldName);
                throw new WeDPRException(
                        "Not found select field index, fieldName: " + selectFieldName);
            }
            indexList.add(index);
        }
        return indexList;
    }

    public static String[] makeNextLine(String[] originalLineList, List<Integer> selectIndexList) {
        String[] nextLine = new String[selectIndexList.size()];
        int curIndex = 0;
        for (Integer index : selectIndexList) {
            nextLine[curIndex++] = originalLineList[index];
        }

        return nextLine;
    }

    public static long makeDatasetToMpcDataDirect(
            String datasetFilePath,
            String datasetMpcFilePath,
            int datasetCountNumber,
            boolean withHeader)
            throws IOException, CsvException, WeDPRException {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(datasetFilePath));
                BufferedWriter bufferedWriter =
                        new BufferedWriter(new FileWriter(datasetMpcFilePath));
                CSVReader csvReader = new CSVReader(bufferedReader);
                CSVWriter csvWriter =
                        new CSVWriter(
                                bufferedWriter,
                                ' ',
                                ICSVWriter.DEFAULT_QUOTE_CHARACTER,
                                ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                ICSVWriter.DEFAULT_LINE_END)) {

            // read original column names
            String[] fieldNames = csvReader.readNextSilently();
            List<String> originalFieldNames = Arrays.asList(fieldNames);

            List<String> newFieldNames = makeFieldNamesNormalized(originalFieldNames);

            List<String> selectFieldNames = makeSelectFields(datasetCountNumber);
            List<Integer> selectFieldsIndex =
                    makeSelectFieldsIndex(selectFieldNames, newFieldNames);

            if (withHeader) {
                // write in new column names
                csvWriter.writeNext(selectFieldNames.toArray(new String[0]), false);
            }

            // read the remaining data rows and write them into the output file
            String[] nextLine;
            long lineNum = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                String[] writeNextLine = makeNextLine(nextLine, selectFieldsIndex);

                if (logger.isTraceEnabled()) {
                    logger.trace("next line: {}", Arrays.asList(writeNextLine));
                }

                csvWriter.writeNext(writeNextLine, false);
                lineNum++;
            }

            logger.info(
                    "lineNum: {}, original field names: {}, field names: {}, select field index: {}",
                    lineNum,
                    originalFieldNames,
                    fieldNames,
                    selectFieldsIndex);

            return lineNum;
        }
    }

    public static String replaceMpcContentFieldHolder(
            String mpcContent, long mpcDatasetRecordCount, List<DatasetInfo> datasetInfoList) {

        mpcContent =
                mpcContent.replace(
                        "$(ppc_max_record_count)", String.valueOf(mpcDatasetRecordCount));

        int partCount = datasetInfoList.size();
        for (int i = 0; i < partCount; i++) {
            FileMeta dataset = datasetInfoList.get(i).getDataset();
            long datasetRecordCount = dataset.getDatasetRecordCount();

            String placeholder = String.format(MPC_SOURCE_RECORD_COUNT_LABEL, i);
            mpcContent = mpcContent.replace(placeholder, String.valueOf(datasetRecordCount));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("new mpc content: {}", mpcContent);
        }

        return mpcContent;
    }

    public static boolean checkNeedRunPsi(String jobId, String mpcContent) {

        String regex = ExecutorConfig.getMpcPsiOptionRegex();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mpcContent);

        boolean needRunPsi = matcher.find();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "check need run psi for mpc, jobID: {}, needRunPsi: {} ", jobId, needRunPsi);
        }

        return needRunPsi;
    }

    public static long mergeAndSortById(
            String datasetFilePath,
            String psiResultFilePath,
            String resultFilePath,
            int datasetCountNumber,
            boolean withHeader)
            throws Exception {

        Set<Integer> psiIdMap = new TreeSet<>();

        // read the id field value of the psi result file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(psiResultFilePath));
                CSVReader psiResultReader = new CSVReader(bufferedReader)) {

            String[] fieldNames = psiResultReader.readNextSilently();
            List<String> originalFieldNames = Arrays.asList(fieldNames);

            int idFieldIndex = getIdFieldIndex(originalFieldNames);

            String[] nextLine;
            while ((nextLine = psiResultReader.readNext()) != null) {

                String strId = nextLine[idFieldIndex];
                if (logger.isTraceEnabled()) {
                    logger.trace("id: {}, next line: {}", strId, Arrays.asList(nextLine));
                }

                psiIdMap.add(Integer.valueOf(strId));
            }
        }

        long lineNum = 0;
        // read the id field value of the psi result file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(datasetFilePath));
                CSVReader datasetFileReader = new CSVReader(bufferedReader);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(resultFilePath));
                CSVWriter resultFileWriter =
                        new CSVWriter(
                                bufferedWriter,
                                ' ',
                                ICSVWriter.DEFAULT_QUOTE_CHARACTER,
                                ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                                ICSVWriter.DEFAULT_LINE_END)) {

            String[] fieldNames = datasetFileReader.readNextSilently();
            List<String> originalFieldNames = Arrays.asList(fieldNames);

            List<String> newFieldNames = makeFieldNamesNormalized(originalFieldNames);

            int idFieldIndex = getIdFieldIndex(originalFieldNames);

            List<String> selectFieldNames = makeSelectFields(datasetCountNumber);
            List<Integer> selectFieldsIndex =
                    makeSelectFieldsIndex(selectFieldNames, newFieldNames);

            if (withHeader) {
                // write in new column names
                resultFileWriter.writeNext(selectFieldNames.toArray(new String[0]), false);
            }

            String[] nextLine;
            while ((nextLine = datasetFileReader.readNext()) != null) {

                String strId = nextLine[idFieldIndex];
                Integer id = Integer.valueOf(strId);
                if (!psiIdMap.contains(id)) {
                    continue;
                }

                String[] writeNextLine = makeNextLine(nextLine, selectFieldsIndex);

                if (logger.isTraceEnabled()) {
                    logger.trace("next line: {}", Arrays.asList(writeNextLine));
                }

                resultFileWriter.writeNext(writeNextLine, false);
                lineNum++;
            }
        }

        return lineNum;
    }
}
