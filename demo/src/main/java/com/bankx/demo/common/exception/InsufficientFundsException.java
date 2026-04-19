package com.bankx.demo.common.exception;

import com.bankx.demo.common.enums.ErrorCode;

public class InsufficientFundsException extends BaseException {

    public InsufficientFundsException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InsufficientFundsException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }

}
