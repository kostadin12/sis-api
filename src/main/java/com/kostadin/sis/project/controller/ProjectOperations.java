package com.kostadin.sis.project.controller;

import com.kostadin.sis.project.model.request.SaveProjectCommand;
import com.kostadin.sis.project.model.request.UpdateProjectCommand;
import com.kostadin.sis.project.model.response.ProjectDTO;
import com.kostadin.sis.project.model.response.ProjectName;
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

@Tag(name = "Project operations",description = """
        Endpoints that allow:
        1. New Project entities to be created, received, updated or deleted from/to the database.
        2. Adding PROJECT scope labels to the Project
        3. Removing a Label from a Project.
        4. Listing Project names by a given filter (autocomplete feature).
        """)
public interface ProjectOperations {
    @Operation(
            summary = "Saves a new project in the DB.",
            description = """
                    Saves a new project in the DB. Method requires name, description, initial project owner (employee number) and capacity mode (boolean).
                    Upon saving, the initial project owner and his system scoped labels are added to the project.
                    
                    Project is returned as a DTO, containing only the project details.
                    
                    Method is used by users with 'MANAGER' or 'ADMIN' role, in order to create a new project in Sis.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid project.", content = @Content)
    @ApiResponse(responseCode = "201", description = "Project saved.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDTO.class)))
    ProjectDTO saveProject(@RequestBody SaveProjectCommand saveProjectCommand);

    @Operation(
            summary = "Receives a list of all projects in the DB.",
            description = """
                    Receives a list of all projects in the DB.
                    Method returns a list, containing only the project id and name.
                    
                    Method is used when displaying the projects in the Project Calendar for 'ADMIN' users only.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Projects received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectName.class))))
    List<ProjectName> getProjects();

    @Operation(
            summary = "Receives a project from the DB by id.",
            description = """
                    Receives a project from the DB by id.
                    Returns a DTO, which contains only the project details, without the members.
                    
                    It is used in the Project Details view - for displaying the project information.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Project received.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDTO.class)))
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    ResponseEntity<ProjectDTO> getProjectById(@Parameter(name = "id", example = "1") long id);

    @Operation(
            summary = "[UNUSED] Autocomplete search for projects.",
            description = """
                    [UNUSED]
                    Retrieves a list of projects by applying the given filter (autocomplete search feature).
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Project received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectName.class))))
    List<ProjectName> findProjects(@Parameter(name = "filter", example = "Teams Information System") String filter);

    @Operation(
            summary = "Updates a project in the DB by id.",
            description = """
                    Updates a project in the DB by id.
                    Method has the ability to change the project owner, selecting a beneficiary which must meet the requirements (must be a manager or admin).
                    
                    Method is used in the Project Details view when updating the project information. Accessible only for 'ADMIN' or 'MANAGER' users.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid project.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Project updated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDTO.class)))
    ProjectDTO updateProject(@Parameter(name = "id", example = "1") long id, @Parameter(name = "projectOwnerId", example = "BB123456") String projectOwnerEmployeeNumber, @RequestBody UpdateProjectCommand updateProjectCommand);

    @Operation(
            summary = "Deletes a project from the DB by id.",
            description = """
                    Deletes a project from the DB by id.
                    When deleting the project, all of the 'PROJECT' scoped labels that belong to it are also deleted from the DB.
                    
                    Accessible only for 'ADMIN' users or by the project owner (MANAGER).
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    @ApiResponse(responseCode = "204", description = "Project deleted.")
    void deleteProject(@Parameter(name = "id", example = "1") long id, @Parameter(name = "projectOwnerId", example = "BB123456") String projectOwnerEmployeeNumber);
}
