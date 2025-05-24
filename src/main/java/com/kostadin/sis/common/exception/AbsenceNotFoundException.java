package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class AbsenceNotFoundException extends CustomResponseStatusException {
    public AbsenceNotFoundException(String message) {
        super(NOT_FOUND, ErrorCode.ABSENCE_NOT_FOUND.getErrorCode(), ErrorCode.ABSENCE_NOT_FOUND.getReason(), message);
    }
}
