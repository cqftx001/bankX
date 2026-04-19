package com.bankx.demo.common.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Unified API responses")
public class ResponseResult<T> {

    @Schema(description = "Business response code", example = "0000")
    private String code;

    @Schema(description = "Response message", example = "Success")
    private String message;

    @Schema(description = "Response payload")
    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Los_Angeles")
    @Schema(description = "Timestamp of the response")
    private LocalDateTime timestamp;

    /**
     * Unique request trace ID.
     * 每次请求的唯一追踪 ID。
     * request.setAttribute("requestId", requestId);
     */
    @Schema(description = "Request trace ID", example = "abc-xyz-123")
    private final String requestId;

    /**
     * 构造函数
     * @param code
     * @param message
     */
    public ResponseResult(String code, String message, String requestId) {
        this.code = code;
        this.message = message;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     * @param code
     * @param message
     * @param data
     */
    public ResponseResult(String code, String message, T data, String requestId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 返回成功消息
     * @return success  message
     */
    public static ResponseResult success(){
        return ResponseResult.success("Success");
    }


    /**
     * Build a success response with payload.
     *
     * @param data response payload
     * @param requestId request trace ID
     * @return success response
     */
    public static <T> ResponseResult<T> success(T data, String requestId) {
        return ResponseResult.<T>builder()
                .code("0000")
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }

    /**
     * Build a success response without payload.
     *
     * @param requestId request trace ID
     * @return success response with null data
     */
    public static <T> ResponseResult<T> success(String requestId) {
        return ResponseResult.<T>builder()
                .code("0000")
                .message("Success")
                .data(null)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }

    /**
     * Build a failure response.
     *
     * @param code business error code
     * @param message response message
     * @param requestId request trace ID
     * @return failure response
     */
    public static <T> ResponseResult<T> fail(String code, String message, String requestId) {
        return ResponseResult.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .build();
    }

    /**
     * Response Template
     * @param <T>

    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult<UserVo>> getUserById(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        UserVo userVo = userService.getUserById(id);

        String requestId = (String) request.getAttribute("requestId");

        return ResponseEntity.ok(ResponseResult.success(userVo, requestId));
    }

     */
}
