package com.shareint.backend.core.exception;

import com.shareint.backend.core.dto.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse<Void>> handleApiException(ApiException ex, WebRequest request) {
        logger.error("ApiException: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(BaseResponse.error(ex.getStatus(), ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.error("ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(BaseResponse.error(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        logger.error("DataIntegrityViolationException: {}", ex.getMostSpecificCause().getMessage());
        String message = "A record with that value already exists.";
        if (ex.getMostSpecificCause().getMessage() != null) {
            if (ex.getMostSpecificCause().getMessage().contains("nid_number")) {
                message = "That NID number is already registered.";
            } else if (ex.getMostSpecificCause().getMessage().contains("phone_number")) {
                message = "That phone number is already registered.";
            } else if (ex.getMostSpecificCause().getMessage().contains("email")) {
                message = "That email address is already registered.";
            }
        }
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(HttpStatus.CONFLICT, message, request.getDescription(false)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.error("ValidationException: {}", details);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(HttpStatus.BAD_REQUEST, "Validation failed: " + details, details));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        logger.error("AccessDeniedException: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(BaseResponse.error(HttpStatus.FORBIDDEN, "Access Denied", request.getDescription(false)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unhandled Exception on {}: ", request.getDescription(false), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getDescription(false)));
    }
}
