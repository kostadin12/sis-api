package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class AbsenceBadRequestException extends CustomResponseStatusException {
    public AbsenceBadRequestException(String message) {
        super(BAD_REQUEST, ErrorCode.ABSENCE_BAD_REQUEST.getErrorCode(), ErrorCode.ABSENCE_BAD_REQUEST.getReason(), message);
    }
}
