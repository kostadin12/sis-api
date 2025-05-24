package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class LabelNotFoundException extends CustomResponseStatusException {
    public LabelNotFoundException(String message) {
        super(NOT_FOUND, ErrorCode.LABEL_NOT_FOUND.getErrorCode(), ErrorCode.LABEL_NOT_FOUND.getReason(), message);
    }
}
