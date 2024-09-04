package com.webank.wedpr.components.publish.helper;

import com.webank.wedpr.components.uuid.generator.WeDPRUuidGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
    }

    @Getter
    @AllArgsConstructor
    public enum InvokeStatus {
        SUCCESS("success"),
        FAILED("failed"),
        EXPIRED("expired");
        private final String value;
    }

    protected static final String PUBLISH_ID_PREFIX = "s-";

    public static final String PUBLISH_PIR_NEED_COLUMN = "id";

    public static String newPublishServiceId() {
        return PUBLISH_ID_PREFIX + WeDPRUuidGenerator.generateID();
    }

}
