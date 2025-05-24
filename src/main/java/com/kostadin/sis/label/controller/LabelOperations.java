package com.kostadin.sis.label.controller;

import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.label.model.request.SaveLabelCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Label operations",description = """
        Endpoints that allow:
        1. New Label entities to be created, received, updated or deleted from/to the database.
        2. Listing Labels filtered by a given name (autocomplete feature).
        """)
public interface LabelOperations {
    @Operation(
            summary = "[UNUSED] Saves a new label",
            description = """
                    [UNUSED]
                    Saves a new label to the DB and automatically sets its scope to 'SYSTEM'.
                    
                    Method is used for creating system scoped labels only.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid label.", content = @Content)
    @ApiResponse(responseCode = "201", description = "Label saved.")
    void saveLabel(@RequestBody SaveLabelCommand saveLabelCommand);

    @Operation(
            summary = "Receives a list of all labels in the DB, filtered by their scope.",
            description = """
                    Receives a list of all labels in the DB, filtered by their scope.
                    
                    Method is used whenever all 'SYSTEM' scoped labels are displayed.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Labels received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LabelDTO.class))))
    ResponseEntity<List<LabelDTO>> getAll(LabelScope labelScope);

    @Operation(
            summary = "[UNUSED] Receives a label from the DB by ID.",
            description = """
                    [UNUSED]
                    Receives a label from the DB by ID.
                    
                    Method is used to display the label information to the FE.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Label not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Label received.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LabelDTO.class)))
    ResponseEntity<LabelDTO> getLabel(@Parameter(name = "id", example = "1") long id);

    @Operation(
            summary = "[UNUSED] Updates an existing label by id.",
            description = """
                    [UNUSED]
                    Updates an existing label by id.
                    Method is used to update 'SYSTEM' scoped labels only.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid label.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Label not found.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Label updated.")
    void updateLabel(@Parameter(name = "id", example = "1") long id, SaveLabelCommand saveLabelCommand);

    @Operation(
            summary = "[UNUSED] Deletes an existing label from the DB by id.",
            description = """
                    [UNUSED]
                    Deletes an existing label from the DB by id.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Label not found.", content = @Content)
    @ApiResponse(responseCode = "204", description = "Label deleted.")
    void deleteLabel(@Parameter(name = "id", example = "1") long id);

    @Operation(
            summary = "Receives a list of labels from the DB - filtered by scope and name.",
            description = """
                    Receives a list of labels from the DB - filtered by scope and name.
                    
                    Method is used when searching for labels by autocomplete.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Labels received.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LabelDTO.class))))
    List<LabelDTO> findLabelsByScopeAndName(LabelScope labelScope, @Schema(name = "filter", description = "Filter for searching labels by autocomplete", example = "Software Developer") String filter);
}
