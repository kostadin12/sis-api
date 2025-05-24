package com.kostadin.sis.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class CustomResponseStatusException extends ResponseStatusException {
    private final HttpStatus status;
    private final String errorCode;
    private final String reason;
    private final String message;

    public CustomResponseStatusException(HttpStatus status,String errorCode, String reason, String message) {
        super(status);
        this.status = status;
        this.errorCode = errorCode;
        this.reason = reason;
        this.message = message;
    }
}
