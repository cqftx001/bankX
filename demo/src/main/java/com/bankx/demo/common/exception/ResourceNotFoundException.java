package com.bankx.demo.common.exception;


import com.bankx.demo.common.enums.ErrorCode;

public class ResourceNotFoundException extends BaseException{

    public ResourceNotFoundException(ErrorCode errorCode){
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String customMessage){
        super(errorCode, customMessage);
    }
}
