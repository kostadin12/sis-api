package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class ExcelReportBadRequestException extends CustomResponseStatusException {
    public ExcelReportBadRequestException(String message) {
        super(BAD_REQUEST, ErrorCode.REPORT_BAD_REQUEST.getErrorCode(), ErrorCode.REPORT_BAD_REQUEST.getReason(), message);
    }
}
