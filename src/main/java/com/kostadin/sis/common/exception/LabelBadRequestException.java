package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class LabelBadRequestException extends CustomResponseStatusException {
    public LabelBadRequestException(String message) {
        super(BAD_REQUEST, ErrorCode.LABEL_BAD_REQUEST.getErrorCode(), ErrorCode.LABEL_BAD_REQUEST.getReason(), message);
    }
}
