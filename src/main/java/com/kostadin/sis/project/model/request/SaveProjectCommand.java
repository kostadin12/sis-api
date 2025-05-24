package com.kostadin.sis.project.model.request;

import com.kostadin.sis.project.model.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class SaveProjectCommand {

    @Schema(name = "name", example = "Teams Information System")
    @Length(min = 2, max = 255, message = "Project name length is between 2 and 255 symbols.")
    @NotBlank(message = "Project name is mandatory.")
    private String name;

    @Schema(name = "description", example = "Project description.")
    @Length(min = 2, max = 255, message = "Project description length is between 2 and 255 symbols.")
    @NotBlank(message = "Project description is mandatory.")
    private String description;

    @Schema(name = "startDate", example = "2020-01-01")
    @NotNull(message = "Project start date is mandatory.")
    private LocalDate startDate;

    private ProjectStatus projectStatus;

    @Schema(name = "initialProjectOwner", example = "BB123456")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    @NotBlank
    private String initialProjectOwner;

    private boolean capacityMode;
}
