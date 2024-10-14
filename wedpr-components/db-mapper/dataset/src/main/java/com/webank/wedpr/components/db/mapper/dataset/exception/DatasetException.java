package com.webank.wedpr.components.db.mapper.dataset.exception;

import com.webank.wedpr.common.utils.WeDPRException;

public class DatasetException extends WeDPRException {

    public DatasetException(int code, String message) {
        super(code, message);
    }

    public DatasetException(String message) {
        super(message);
    }

    public DatasetException(Throwable cause) {
        super(cause);
    }

    public DatasetException(String message, Throwable cause) {
        super(message, cause);
    }
}
