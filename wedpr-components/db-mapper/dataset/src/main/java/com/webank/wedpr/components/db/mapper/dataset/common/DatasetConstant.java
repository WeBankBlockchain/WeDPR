package com.webank.wedpr.components.db.mapper.dataset.common;

import com.webank.wedpr.components.db.mapper.dataset.exception.DatasetException;
import com.webank.wedpr.core.utils.Constant;

public class DatasetConstant {
    public static final String MAX_DATASET_PERMISSION_EXPIRED_TIME = "9999-12-31";

    public static final String DATASET_LABEL = "dataset";

    public static final String DATASET_ID_PREFIX = "d-";
    public static final String DATASET_PERMISSION_ID_PREFIX = "p-";

    public static final String WEDPR_DATASET_API_PREFIX =
            Constant.WEDPR_API_PREFIX + "/" + DATASET_LABEL;

    public enum DatasetVisibilityType {
        PRIVATE(0),
        PUBLIC(1);

        private final int value;

        DatasetVisibilityType(int value) {
            this.value = value;
        }

        public static void isValidDatasetVisibility(int value) throws DatasetException {
            DatasetVisibilityType[] values = DatasetVisibilityType.values();

            boolean valid = false;
            for (DatasetVisibilityType datasetVisibility : values) {
                if (value == datasetVisibility.value) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                throw new DatasetException("Unsupported dataset visibility value, value: " + value);
            }
        }

        public int getValue() {
            return value;
        }
    }

    // Permission type:
    //      readable
    //      writable
    //      visible
    //      usable
    public enum DatasetPermissionType {
        VISIBLE(1, "visible"),
        WRITABLE(2, "writable"),
        READABLE(3, "readable"),
        USABLE(4, "usable");

        private final int type;
        private final String strType;

        DatasetPermissionType(int type, String strType) {
            this.type = type;
            this.strType = strType;
        }

        public static void isValidDatasetPermissionType(int value) throws DatasetException {
            DatasetPermissionType[] values = DatasetPermissionType.values();

            boolean valid = false;
            for (DatasetPermissionType datasetPermissionType : values) {
                if (value == datasetPermissionType.type) {
                    valid = true;
                    break;
                }
            }

            if (!valid) {
                throw new DatasetException("Invalid dataset permissionType, value: " + value);
            }
        }

        public static int fromString(String strType) throws DatasetException {
            DatasetPermissionType[] values = DatasetPermissionType.values();
            for (DatasetPermissionType datasetPermissionType : values) {
                if (datasetPermissionType.getStrType().equals(strType)) {
                    return datasetPermissionType.getType();
                }
            }

            throw new DatasetException("Invalid string dataset permissionType, value: " + strType);
        }

        public int getType() {
            return type;
        }

        public String getStrType() {
            return strType;
        }
    }

    // Permission scope
    public enum DatasetPermissionScope {
        GLOBAL("global"),
        AGENCY("agency"),
        USER_GROUP("user_group"),
        USER("user");

        private final String value;

        DatasetPermissionScope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
