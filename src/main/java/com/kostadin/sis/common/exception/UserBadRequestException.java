package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class UserBadRequestException extends CustomResponseStatusException {
    public UserBadRequestException(String message) {
        super(BAD_REQUEST, ErrorCode.PROJECT_BAD_REQUEST.getErrorCode(), ErrorCode.PROJECT_BAD_REQUEST.getReason(), message);
    }
}
