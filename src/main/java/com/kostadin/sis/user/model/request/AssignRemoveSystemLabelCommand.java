package com.kostadin.sis.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AssignRemoveSystemLabelCommand {
    @Schema(name = "employeeNumber", example = "BB123456")
    @NotBlank(message = "Employee number is mandatory.")
    private String employeeNumber;

    @Schema(name = "labelName", example = "Software Developer")
    @NotBlank(message = "Label name is mandatory.")
    private String labelName;
}
