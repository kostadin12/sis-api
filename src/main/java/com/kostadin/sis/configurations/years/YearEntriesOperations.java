package com.kostadin.sis.configurations.years;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Year entries operations",description = """
        Storing year entries and configuration information about each year.
        """)
public interface YearEntriesOperations {
    @Operation(
            summary = "Receives all non-working days by year.",
            description = """
                    Receives all non-working days by year.
                    If a year entry is non-existent in the DB, an external API would be called and a new year entry would be saved to the DB.
                    
                    Method is used when logging in, to receive all non-working days (holidays and weekends), in order to display the in the Personal Calendar and Project Calendar views.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.")
    @ApiResponse(responseCode = "403", description = "Forbidden request.")
    @ApiResponse(responseCode = "200", description = "Received non-working days.", content = @Content(schema = @Schema(example = """
            [
                "2020-12-24", "2020-12-25"
            ]
            """)))
    List<String> getNonWorkingDays(@RequestBody ReceiveNonWorkingDaysCommand receiveNonWorkingDaysCommand);
}
