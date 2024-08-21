package com.webank.wedpr.components.dataset.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.webank.wedpr.components.dataset.datasource.DBType;
import com.webank.wedpr.components.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.dataset.exception.DatasetException;
import com.webank.wedpr.core.utils.Common;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wedpr.dataset")
@Getter
public class DataSourceTypeConfig {

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LabelValue {
        private String label;
        private String value;
        private boolean supportDynamicType = false;
        private List<LabelValue> children;
    }

    private final List<LabelValue> dataSourceType = new ArrayList<>();

    @Bean
    public void validateDataSourceConfigurations() throws DatasetException {

        for (LabelValue labelValue : dataSourceType) {

            String label = labelValue.getLabel();
            String value = labelValue.getValue();
            // boolean supportDynamicType = labelValue.isSupportDynamicType();
            Common.requireNonEmpty("label", label);
            Common.requireNonEmpty("value", value);

            DataSourceType.isValidDataSourceType(value);

            if (!value.equalsIgnoreCase(DataSourceType.DB.name())) {
                continue;
            }

            // check db types
            for (LabelValue childLabelValue : labelValue.getChildren()) {
                DBType.isSupportedDBType(childLabelValue.getValue());
            }
        }
    }
}
