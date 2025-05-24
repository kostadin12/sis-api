package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ConfigurationNotFoundException extends CustomResponseStatusException {
    public ConfigurationNotFoundException (String key) {
        super(NOT_FOUND, ErrorCode.CONFIGURATION_NOT_FOUND.getErrorCode(), ErrorCode.CONFIGURATION_NOT_FOUND.getReason(), "Configuration with key " + key + " not found.");
    }
}
