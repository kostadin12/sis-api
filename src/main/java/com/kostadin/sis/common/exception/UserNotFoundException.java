package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class UserNotFoundException extends CustomResponseStatusException {
    public UserNotFoundException(String message) {
        super(NOT_FOUND, ErrorCode.USER_NOT_FOUND.getErrorCode(), ErrorCode.USER_NOT_FOUND.getReason(), message);
    }
}
