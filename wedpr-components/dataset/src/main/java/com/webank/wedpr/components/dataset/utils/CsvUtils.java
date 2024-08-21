package com.webank.wedpr.components.dataset.utils;

import com.opencsv.CSVReaderHeaderAware;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvUtils {

    private static final Logger logger = LoggerFactory.getLogger(CsvUtils.class);

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
                CSVReaderHeaderAware csvReaderHeaderAware =
                        new CSVReaderHeaderAware(bufferedReader)) {
            Map<String, String> header = csvReaderHeaderAware.readMap();

            List<String> fieldList = new ArrayList<>();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                fieldList.add(entry.getKey());
            }

            String joinString = String.join(",", fieldList);
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
     * @param csvFilePath
     * @param sheetNum
     * @throws IOException
     */
    public static void convertExcelToCsv(String excelFilePath, String csvFilePath, int sheetNum)
            throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();
        logger.info(
                "try to convert excel to csv, excelPath: {}, sheetNum: {}",
                excelFilePath,
                sheetNum);

        // Create an output stream for writing to a CSV file.
        // read excel with apache POI
        try (FileWriter fileWriter = new FileWriter(csvFilePath);
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
                    csvWriter.print(',');
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
}
