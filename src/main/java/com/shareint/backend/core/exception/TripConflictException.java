package com.shareint.backend.core.exception;

import org.springframework.http.HttpStatus;

public class TripConflictException extends ApiException {
    public TripConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
