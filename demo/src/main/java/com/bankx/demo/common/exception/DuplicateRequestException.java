package com.bankx.demo.common.exception;

import com.bankx.demo.common.enums.ErrorCode;

public class DuplicateRequestException extends BaseException {

    public DuplicateRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateRequestException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

}
