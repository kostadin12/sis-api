package com.kostadin.sis.exception;

import com.kostadin.sis.exception.custom.CustomResponseStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ExceptionBody handleDefaultException(Exception e) {
        log.error(e.getMessage());
        return new ExceptionBody()
                .setStatus(INTERNAL_SERVER_ERROR)
                .setErrorCode("ERROR")
                .setMessage(e.getMessage());
    }

    @ExceptionHandler({CustomResponseStatusException.class})
    public ResponseEntity<ExceptionBody> handleCustomResponseStatusException(CustomResponseStatusException e) {
        log.error(e.getMessage());
        var exceptionBody = new ExceptionBody()
                .setStatus(e.getStatus())
                .setErrorCode(e.getErrorCode())
                .setReason(e.getReason())
                .setMessage(e.getMessage());

        return ResponseEntity.status(e.getStatus()).body(exceptionBody);
    }

    @ResponseBody
    @ResponseStatus(CONFLICT)
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ExceptionBody handleUniqueEntityException(DataIntegrityViolationException e){
        log.error("Error persisting changes in the database. Cause: {}", e.getMessage(), e);
        return new ExceptionBody()
                .setStatus(CONFLICT)
                .setReason("SQL UNIQUE CONSTRAINT EXCEPTION")
                .setMessage("Entity already exists.")
                .setErrorCode(ErrorCode.DATABASE_BAD_REQUEST.getErrorCode());
    }

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionBody handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);

        var message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(joining(","));

        return new ExceptionBody()
                .setStatus(BAD_REQUEST)
                .setErrorCode(ErrorCode.FIELDS_INVALID.getErrorCode())
                .setReason(ErrorCode.FIELDS_INVALID.getReason())
                .setMessage(message);
    }
}
