package com.shareint.backend.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private final Instant timestamp = Instant.now();

    public static <T> BaseResponse<T> success(T data, String message) {
        return BaseResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> BaseResponse<T> error(HttpStatus status, String message, String details) {
        return BaseResponse.<T>builder()
                .success(false)
                .message(message)
                .error(new ErrorDetails(status.value(), status.getReasonPhrase(), details))
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class ErrorDetails {
        private int code;
        private String status;
        private String details;
    }
}
