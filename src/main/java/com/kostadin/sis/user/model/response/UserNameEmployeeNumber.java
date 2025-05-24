package com.kostadin.sis.user.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserNameEmployeeNumber {
    @Schema(name = "firstName", example = "John")
    private String firstName;
    @Schema(name = "lastName", example = "Doe")
    private String lastName;
    @Schema(name = "employeeNumber", example = "BB123456")
    private String employeeNumber;
    }
