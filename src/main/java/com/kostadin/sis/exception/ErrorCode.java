package com.kostadin.sis.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    PROJECT_NOT_FOUND("ERR001","Project not found."),
    PROJECT_BAD_REQUEST("ERR002","Project bad request."),
    USER_NOT_FOUND("ERR003","User not found."),
    USER_BAD_REQUEST("ERR004","User bad request."),
    LABEL_NOT_FOUND("ERR005","Label not found."),
    LABEL_BAD_REQUEST("ERR006", "Label bad request."),
    ABSENCE_NOT_FOUND("ERR007","Absence not found."),
    DATABASE_BAD_REQUEST("ERR008", "SQL UNIQUE CONSTRAINT EXCEPTION."),
    FIELDS_INVALID("ERR009","Invalid field(s)."),
    URI_VARIABLE_BAD_REQUEST("ERR010","URI variables cannot be null."),
    CONFIGURATION_NOT_FOUND("ERR011", "Configuration not found."),
    ABSENCE_BAD_REQUEST("ERR012","Absence bad request."),
    REPORT_BAD_REQUEST("ERR013", "Could not generate report."),
    TOKEN_REQUEST("ERR014","Couldn't fetch token."),
    SUBSCRIPTION_BAD_REQUEST("ERR015", "Subscription bad request.");

    private final String errorCode;
    private final String reason;
}
