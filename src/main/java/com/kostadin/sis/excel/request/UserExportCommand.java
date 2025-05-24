package com.kostadin.sis.excel.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class UserExportCommand {
    @Schema(name = "employeeNumbers", description = "Employee numbers of exported users", example = """
            [
                "BB123456", "BB654321"
            ]
            """)
    List<String> employeeNumbers;
    @Schema(name = "startDate", example = "2020-01-01")
    @NotNull(message = "Start date is mandatory.")
    LocalDate startDate;
    @Schema(name = "endDate", example = "2020-02-01")
    @NotNull(message = "End date is mandatory.")
    LocalDate endDate;
}
