package com.kostadin.sis.project.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class GetUsersAbsencesByLabelsCommand {
    @Schema(name = "projectId", example = "1")
    @NotNull(message = "Project ID is mandatory.")
    Long projectId;

    @Schema(name = "startDate", example = "2020-01-01")
    @NotNull(message = "Start date is mandatory.")
    LocalDate startDate;

    @Schema(name = "endDate", example = "2020-02-01")
    @NotNull(message = "End date is mandatory.")
    LocalDate endDate;

    @Schema(name = "labels", example = """
            [
                "Software Developer", "Back-end Developer"
            ]
            """)
    List<String> labels;
}
