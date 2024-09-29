package com.webank.wedpr.components.dataset.utils;

import com.opencsv.CSVReader;
import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.category.DBDataSource;
import com.webank.wedpr.components.dataset.sqlutils.SQLExecutor;
import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvUtils {

    private static final Logger logger = LoggerFactory.getLogger(CsvUtils.class);

    private static final String CSV_SEPARATOR = ",";

    private CsvUtils() {}

    /**
     * read csv file header
     *
     * @param csvPath
     * @return
     * @throws DatasetException
     */
    public static List<String> readCsvHeader(String csvPath) throws DatasetException {

        try (BufferedReader bufferedReader =
                        Files.newBufferedReader(Paths.get(csvPath), StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(bufferedReader)) {

            String[] headers = csvReader.readNextSilently();
            List<String> fieldList = Arrays.asList(headers);

            String joinString = String.join(CSV_SEPARATOR, fieldList);
            logger.info(
                    "read csv header, fields count: {}, field list: {}, csvPath: {}",
                    fieldList.size(),
                    joinString,
                    csvPath);

            return fieldList;

        } catch (Exception e) {
            logger.error("read csv file header exception, csvPath: {}, e: ", csvPath, e);
            throw new DatasetException("Failed to read csv header, e: " + e.getMessage());
        }
    }

    /**
     * convert excel file to csv
     *
     * @param excelFilePath
     * @param outputCsvFilePath
     * @param sheetNum
     * @throws IOException
     */
    public static void convertExcelToCsv(
            String excelFilePath, String outputCsvFilePath, int sheetNum) throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();
        logger.info(
                "try to convert excel to csv, excelPath: {}, sheetNum: {}",
                excelFilePath,
                sheetNum);

        // Create an output stream for writing to a CSV file.
        // read excel with apache POI
        try (FileWriter fileWriter = new FileWriter(outputCsvFilePath);
                PrintWriter csvWriter = new PrintWriter(fileWriter);
                Workbook workbook = WorkbookFactory.create(new File(excelFilePath))) {

            // default to using the first worksheet.
            Sheet sheet = workbook.getSheetAt(sheetNum);

            // iterate through each row of the worksheet.
            for (Row row : sheet) {
                // iterate through each cell in the row.
                for (Cell cell : row) {
                    // read data based on the cell type.
                    switch (cell.getCellType()) {
                        case STRING:
                            csvWriter.print(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            csvWriter.print(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            csvWriter.print(cell.getBooleanCellValue());
                            break;
                        case BLANK:
                            csvWriter.print(" ");
                            break;
                        default:
                            String cellValue = cell.getStringCellValue();
                            logger.error(
                                    "unrecognized cell type in excel, excel: {}, cellType: {}, cellValue: {}",
                                    excelFilePath,
                                    cell.getCellType(),
                                    cellValue);
                            throw new DatasetException(
                                    "Unrecognized cell type in excel, cellType: "
                                            + cell.getCellType()
                                            + " ,cellValue: "
                                            + cellValue);
                    }
                    // add a comma separator after each cell.
                    csvWriter.print(CSV_SEPARATOR);
                }

                // add a newline at the end of each row
                csvWriter.println();
            }
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "convert excel to csv exception, excel: {}, cost(ms)： {}, e: ",
                    excelFilePath,
                    endTimeMillis - startTimeMillis,
                    e);
            throw new DatasetException("Failed to convert excel to csv, e: " + e.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                "convert excel to csv success, excelPath: {}, sheetNum: {}, cost(ms)： {}",
                excelFilePath,
                sheetNum,
                (endTimeMillis - startTimeMillis));
    }

    /**
     * load data from database and write to csv file
     *
     * @param dbType
     * @param dbDataSource
     * @throws DatasetException
     */
    public static void convertDBDataToCsv(
            DBType dbType, DBDataSource dbDataSource, String outputCsvFilePath)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();
        logger.info(
                "try to convert db data to csv, dbType: {}, dbDataSource: {}, outputCsvFilePath: {}",
                dbType,
                dbDataSource,
                outputCsvFilePath);

        final boolean[] bFirst = {true};

        // Create an output stream for writing to a CSV file.
        try (FileWriter fileWriter = new FileWriter(outputCsvFilePath);
                PrintWriter csvWriter = new PrintWriter(fileWriter)) {

            SQLExecutor sqlExecutor = new SQLExecutor();
            sqlExecutor.executeSQL(
                    dbType,
                    dbDataSource,
                    (fields, rowValues) -> {
                        if (bFirst[0]) {
                            bFirst[0] = false;
                            // write header
                            for (int i = 0; i < fields.size(); ++i) {
                                csvWriter.write(fields.get(i));

                                if (i < fields.size() - 1) {
                                    // add a comma separator after each cell.
                                    csvWriter.print(CSV_SEPARATOR);
                                }
                            }

                            // add a newline at the end of each row
                            csvWriter.println();
                        }

                        if (rowValues == null) {
                            return;
                        }

                        // write line values
                        for (int i = 0; i < rowValues.size(); ++i) {
                            csvWriter.write(rowValues.get(i));
                            if (i < rowValues.size() - 1) {
                                // add a comma separator after each cell.
                                csvWriter.print(CSV_SEPARATOR);
                            }
                        }

                        // add a newline at the end of each row
                        csvWriter.println();
                    });

        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "convert db data to csv exception, dbType: {}, dbDataSource: {}, outputCsvFilePath: {}, cost(ms)： {}, e",
                    dbType,
                    dbDataSource,
                    outputCsvFilePath,
                    endTimeMillis - startTimeMillis,
                    e);
            throw new DatasetException("Failed to convert db data to csv, e: " + e.getMessage());
        }

        long endTimeMillis = System.currentTimeMillis();

        logger.info(
                "convert db data to csv success, dbType: {}, dbDataSource: {}, outputCsvFilePath: {}, cost(ms)： {}",
                dbType,
                dbDataSource,
                outputCsvFilePath,
                (endTimeMillis - startTimeMillis));
    }
}
