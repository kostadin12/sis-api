package com.kostadin.sis.absence.controller;

import com.kostadin.sis.absence.model.request.SaveAbsenceCommand;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Absence operations",description = """
        Endpoints that allow new Absence entities to be created, received, updated or deleted from/to the database.
        """)
public interface AbsenceOperations {
    @Operation(
            summary = "Creates a new absence and assign it to a given user.",
            description = """
                    Creates a new absence and assigns it to a given user.
                    
                    Request body: The required fields for saving an absence are start date, end date and type of absence. There is also an option to choose a substitute employee for the period.
                    
                    Absence will be validated before saving, ensuring that it does not overlap with any of the user's current absences.
                    Upon saving, first, an email notification is sent to all users that participate in the same project(s) as the absent user. Then, an Outlook appointment is sent to the absent user's subscribers.
                    
                    Method is used in the Personal Calendar view. When the 'new absence' button is clicked, a modal will appear, where the required information about the absence must be filled.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid absence.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "201", description = "Absence created successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AbsenceDTO.class)))
    AbsenceDTO saveAbsence(@RequestBody SaveAbsenceCommand saveAbsenceCommand);

    @Operation(
            summary = "Receives an absence by id.",
            description = """
                    Receives an absence from the DB by id.
                    Query param: ID of the wanted absence.
                    
                    Method is used when selecting an existent absence from the calendar. The given information would be displayed in the modal, and the user has the option to update it.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Absence not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Absence found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AbsenceDTO.class)))
    ResponseEntity<AbsenceDTO> getAbsenceById(@Parameter(name = "id", description = "Absence id", example = "1") long id);
    @Operation(
            summary = "Updates absence by id.",
            description = """
                    Updates an existing absence in the DB by id.
                    
                    Request body: SaveAbsenceCommand - includes updated absence information.
                    
                    Absence will be validated before saving, ensuring that it does not overlap with any of the user's current absences.
                    Upon saving, an email notification is sent to all users (including the substitute, if any) that participate in the same project(s) as the absent user, notifying them for the updated absence.
                    
                    Method is used in the Personal Calendar view. By clicking on an existent absence, a modal would appear where the user could update his absence.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Absence not found.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Absence updated successfully.")
    void updateAbsence(@Parameter(name = "id", description = "Absence id", example = "1") long id, @RequestBody SaveAbsenceCommand saveAbsenceCommand);
    @Operation(
            summary = "Deletes absence by id.",
            description = """
                    Deletes an existing absence from the DB by id.
                    
                    Method is used in the Personal Calendar view. By clicking on an existent absence, the user has the option to delete it by clicking the 'delete' button.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Absence not found.", content = @Content)
    @ApiResponse(responseCode = "204", description = "Absence deleted successfully.")
    void deleteAbsence(@Parameter(name = "id", description = "Absence id", example = "1") long id, @Parameter(name = "employeeNumber", description = "Employee number the absence belongs to", example = "EMP12345") String employeeNumber);
}
