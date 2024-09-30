package com.webank.wedpr.components.dataset.datasource.dispatch;

import com.webank.wedpr.components.dataset.datasource.processor.CsvDataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.DBDataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.DataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.HdfsDataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.HiveDataSourceProcessor;
import com.webank.wedpr.components.dataset.datasource.processor.XlsxDataSourceProcessor;
import com.webank.wedpr.components.db.mapper.dataset.datasource.DataSourceType;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataSourceProcessorDispatcher {

    private static final Logger logger =
            LoggerFactory.getLogger(DataSourceProcessorDispatcher.class);

    static {
        // pre check
        DataSourceProcessorDispatcher dataSourceProcessorDispatcher =
                new DataSourceProcessorDispatcher();
        DataSourceType[] values = DataSourceType.values();
        for (DataSourceType dataSourceType : values) {
            String name = dataSourceType.name();
            dataSourceProcessorDispatcher.getDataSourceProcessor(name);
        }
    }

    public DataSourceProcessorDispatcher() {

        registerDataSourceProcessor(DataSourceType.CSV.name(), CsvDataSourceProcessor.class);
        registerDataSourceProcessor(DataSourceType.EXCEL.name(), XlsxDataSourceProcessor.class);
        registerDataSourceProcessor(DataSourceType.DB.name(), DBDataSourceProcessor.class);
        registerDataSourceProcessor(DataSourceType.HDFS.name(), HdfsDataSourceProcessor.class);
        registerDataSourceProcessor(DataSourceType.HIVE.name(), HiveDataSourceProcessor.class);
    }

    private final Map<String, Class<?>> dataSourcePreprocessorMap = new HashMap<>();

    public int dataSourceProcessorCount() {
        return dataSourcePreprocessorMap.size();
    }

    public void registerDataSourceProcessor(String name, Class<?> cls) {
        this.dataSourcePreprocessorMap.put(name, cls);
    }

    public DataSourceProcessor getDataSourceProcessor(String dataSourceType) {
        String upperCase = dataSourceType.toUpperCase();
        Class<?> aClass = dataSourcePreprocessorMap.get(upperCase);
        DataSourceProcessor dataSourceProcessor = null;
        try {
            //            dataSourceProcessor = (DataSourceProcessor) aClass.newInstance();
            dataSourceProcessor =
                    (DataSourceProcessor) aClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("newInstance Exception, dataSourceType: {}, e: ", dataSourceType, e);
            throw new RuntimeException(e);
        }
        return dataSourceProcessor;
    }
}
