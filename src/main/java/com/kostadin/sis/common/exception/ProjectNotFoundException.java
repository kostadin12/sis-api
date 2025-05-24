package com.kostadin.sis.common.exception;

import com.kostadin.sis.exception.ErrorCode;
import com.kostadin.sis.exception.custom.CustomResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ProjectNotFoundException extends CustomResponseStatusException {
    public ProjectNotFoundException(long projectId) {
        super(NOT_FOUND, ErrorCode.PROJECT_NOT_FOUND.getErrorCode(), ErrorCode.PROJECT_NOT_FOUND.getReason(), "Project with id " + projectId + " not found.");
    }
}
