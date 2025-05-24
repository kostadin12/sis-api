package com.kostadin.sis.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthCode {

    @Schema(name = "code", example = "asd-123-zxc-456")
    @NotBlank(message = "Code cannot be empty.")
    private String code;
}
