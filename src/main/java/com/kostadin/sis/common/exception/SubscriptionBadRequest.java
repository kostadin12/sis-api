package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class SubscriptionBadRequest extends CustomResponseStatusException {
    public SubscriptionBadRequest(String message) {
        super(BAD_REQUEST, ErrorCode.SUBSCRIPTION_BAD_REQUEST.getErrorCode(), ErrorCode.SUBSCRIPTION_BAD_REQUEST.getReason(), message);
    }
}
