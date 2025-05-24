package com.kostadin.sis.project.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class RemoveUserFromProjectCommand {
    @Schema(name = "employeeId", example = "BB123456")
    @NotBlank(message = "Employee number is mandatory.")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    private String employeeId;

    @Schema(name = "beneficiaryManagerEmployeeNumber", description = "Required only if the project owner is being removed.", example = "BB654321")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    private String beneficiaryManagerEmployeeNumber;
}
