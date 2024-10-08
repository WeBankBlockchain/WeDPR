package com.webank.wedpr.components.publish.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zachma
 * @date 2024/8/31
 */
public class PublishServiceHelper {

    @Getter
    @AllArgsConstructor
    public enum PublishType {
        PIR("pir"),
        XGB("xgb"),
        LR("lr");
        private final String type;

        public static PublishType deserialize(String type) {
            if (StringUtils.isBlank(type)) {
                return null;
            }
            for (PublishType publishType : PublishType.values()) {
                if (publishType.type.compareToIgnoreCase(type) == 0) {
                    return publishType;
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum InvokeStatus {
        SUCCESS("success"),
        FAILED("failed"),
        EXPIRED("expired");
        private final String value;
    }
}
