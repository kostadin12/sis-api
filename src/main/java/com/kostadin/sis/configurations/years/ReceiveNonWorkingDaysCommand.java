package com.kostadin.sis.configurations.years;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReceiveNonWorkingDaysCommand {
    @Schema(name = "year", example = "2025")
    @Pattern(regexp = "[0-9]{4}", message = "Year must contain 4 digits only.")
    @NotBlank(message = "Year cannot be blank.")
    private String year;
}
