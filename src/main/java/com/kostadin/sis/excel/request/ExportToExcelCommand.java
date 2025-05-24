package com.kostadin.sis.excel.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExportToExcelCommand {
    @Schema(name = "projectId", example = "1")
    @NotNull(message = "Project ID is mandatory.")
    Long projectId;
    @Schema(name = "startDate", example = "2020-01-01")
    @NotNull(message = "Start date is mandatory.")
    LocalDate startDate;
    @Schema(name = "endDate", example = "2020-02-01")
    @NotNull(message = "End date is mandatory.")
    LocalDate endDate;
    @Schema(name = "periodCapacityCap", description = "Maximum percentage of available days for all employees in export.", example = "80")
    @Min(0)
    @Max(100)
    int periodCapacityCap;

    @Schema(name = "labels", description = "Optional field for filtering employees by labels.", example = """
            [
                "Back-end Developer", "Front-end Developer"
            ]
            """)
    List<String> labels;
    @Schema(name = "employeeNumbers", description = "Optional field for filtering by employee number.", example = """
            [
                "BB123456", "BB654321"
            ]
            """)
    List<String> employeeNumbers;

    @JsonIgnore
    public boolean areDatesInvalid() {
        return getStartDate().isAfter(getEndDate());
    }
}
