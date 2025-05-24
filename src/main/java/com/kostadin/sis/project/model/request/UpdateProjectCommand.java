package com.kostadin.sis.project.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kostadin.sis.project.model.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class UpdateProjectCommand {

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

    @Schema(name = "endDate", example = "null")
    private LocalDate endDate;

    @NotNull(message = "Project status is mandatory.")
    private ProjectStatus projectStatus;
    @Schema(name = "projectLabels", description = "Names of updated PROJECT scope labels.", example = """
            [
                "Back-end Developer", "Front-end Developer"
            ]
            """)

    private List<String> projectLabels;
    @Schema(name = "newBeneficiary", description = "Employee number of new project owner.", example = "EMP654321")

    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    private String newBeneficiary;

    private boolean capacityMode;

    /**
     * Checks whether the start/end project date range is invalid.
     *
     * @return true if startDate is after endDate, otherwise false
     * @implNote It will return false if endDate is <code>null</code>
     */
    @JsonIgnore
    public boolean isDateRangeInvalid() {
        return endDate != null && startDate.isAfter(endDate);
    }
}
