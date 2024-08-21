package com.webank.wedpr.components.dataset.datasource.processor;

import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.datasource.category.UploadChunkDataSource;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.components.dataset.utils.CsvUtils;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XlsxDataSourceProcessor extends CsvDataSourceProcessor {

    private static final Logger logger = LoggerFactory.getLogger(XlsxDataSourceProcessor.class);

    @Override
    public void prepareData() throws DatasetException {

        long startTimeMillis = System.currentTimeMillis();

        super.prepareData();

        DatasetConfig datasetConfig = dataSourceProcessorContext.getDatasetConfig();
        String mergedFilePath = dataSourceProcessorContext.getMergedFilePath();

        UploadChunkDataSource uploadChunkDataSource =
                (UploadChunkDataSource) dataSourceProcessorContext.getDataSourceMeta();

        File file = new File(mergedFilePath);
        String directoryPath = file.getParent();
        String fileName = file.getName();

        int excelDefaultSheet = datasetConfig.getExcelDefaultSheet();
        String cvsFilePath = directoryPath + File.separator + fileName + ".csv";
        // Notice: convert .xlsx to .csv
        CsvUtils.convertExcelToCsv(mergedFilePath, cvsFilePath, excelDefaultSheet);

        dataSourceProcessorContext.setCvsFilePath(cvsFilePath);

        String datasetId = uploadChunkDataSource.getDatasetId();
        long endTimeMillis = System.currentTimeMillis();
        logger.info(
                " ==> data source processor stage prepare data end merge chunk data, datasetId: {}, mergedFilePath: {}, cvsFilePath: {}, cost(ms): {}",
                datasetId,
                mergedFilePath,
                cvsFilePath,
                endTimeMillis - startTimeMillis);
    }
}
