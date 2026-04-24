package com.bankx.demo.common.exception;


import com.bankx.demo.common.base.ResponseResult;
import com.bankx.demo.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.bankx.demo.common.utils.RequestUtils;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle all custom business exceptions.
     *
     * @param baseException business exception
     * @param request current HTTP request
     * @return standardized failure response
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseResult<Void>> handleBaseException(BaseException baseException,
                                                                    HttpServletRequest request){
        ErrorCode errorCode = baseException.getErrorCode();
        String requestId = RequestUtils.getOrCreateRequestId(request);

        log.warn("Business exception occurred. requestId={}, code={}, message={}",
                requestId,
                errorCode.getCode(),
                baseException.getMessage()
        );

        return ResponseEntity.
                status(errorCode.getHttpStatus())
                .body(ResponseResult.fail(errorCode.getCode(), baseException.getMessage(), requestId));
    }


    /**
     * Handle bean validation errors for @RequestBody.
     *
     * @param ex validation exception
     * @param request current HTTP request
     * @return standardized failure response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseResult<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));


        log.warn(
                "Request body validation failed. requestId={}, message={}",
                requestId,
                message
        );

        return ResponseEntity
                .badRequest()
                .body(ResponseResult.fail(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        message,
                        requestId
                ));
    }
    /**
     * Handle validation errors for request parameters/path variables.
     *
     * @param ex constraint violation exception
     * @param request current HTTP request
     * @return standardized failure response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseResult<Void>> handleConstraintViolationException(Exception ex, HttpServletRequest request){
        String requestId = RequestUtils.getOrCreateRequestId(request);
        log.warn(
                "Request parameter validation failed. requestId={}, message={}",
                requestId,
                ex.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(ResponseResult.fail(
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        ex.getMessage(),
                        requestId
                ));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ResponseResult<Void>> handleOptimisticLockException(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        log.warn("Optimistic lock conflict. requestId={}, message={}",
                requestId, ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseResult.fail(
                        ErrorCode.OPTIMISTIC_LOCK_CONFLICT.getCode(),
                        "Transaction conflict, please try again",
                        requestId
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseResult<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String requestId = RequestUtils.getOrCreateRequestId(request);

        // 判断是否是幂等key冲突
        if (ex.getMessage() != null && ex.getMessage().contains("idempotency_key")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseResult.fail(
                            ErrorCode.DUPLICATE_REQUEST.getCode(),
                            "Duplicate transaction: idempotency key already used",
                            requestId
                    ));
        }

        // 其他数据库约束冲突
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseResult.fail(
                        ErrorCode.DUPLICATE_REQUEST.getCode(),
                        "Data conflict",
                        requestId
                ));
    }

    /**
     * Handle all uncaught exceptions.
     *
     * @param ex unexpected exception
     * @param request current HTTP request
     * @return standardized internal server error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<Void>> handleException(Exception ex, HttpServletRequest request){

        String requestId = RequestUtils.getOrCreateRequestId(request);
        log.error("Unhandled exception occurred. requestId={}, message={}",
                requestId,
                ex
        );

        return ResponseEntity
                .internalServerError()
                .body(ResponseResult.fail(
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                        requestId
                ));
    }
}
