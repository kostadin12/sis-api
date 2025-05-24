package com.kostadin.sis.exception;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class ExceptionBody {

    private HttpStatus status;
    private String errorCode;
    private String reason;
    private String message;
}
