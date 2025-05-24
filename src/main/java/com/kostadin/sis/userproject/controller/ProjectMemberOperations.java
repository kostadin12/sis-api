package com.kostadin.sis.userproject.controller;

import com.kostadin.sis.project.model.request.GetUsersAbsencesByLabelsCommand;
import com.kostadin.sis.project.model.request.RemoveUserFromProjectCommand;
import com.kostadin.sis.project.model.response.ProjectName;
import com.kostadin.sis.userproject.model.ProjectMember;
import com.kostadin.sis.userproject.model.ProjectMemberWithAbsences;
import com.kostadin.sis.userproject.model.ProjectMemberWithCapacities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Project members operations",description = """
        Endpoints that allow:
        1. Adding Users to the Project (including their SYSTEM scope labels).
        2. Removing a User from the Project.
        3. Assigning PROJECT scope labels to Users included in said Project.
        4. Listing Users in a specific Project with their corresponding Labels (that the Project also includes).
        5. Listing Users in a specific Project, including their Absences for a certain period of time.
        """)
public interface ProjectMemberOperations {
    @Operation(
            summary = "Receives project members.",
            description = """
                    Receives a list of all participants by project id. Method returns a list of users with their capacity for the current project.
                    
                    Used in the Project Details view, to display information in the members table.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectMember.class))))
    List<ProjectMember> getUsers(@Parameter(name = "id", example = "1") long id);

    @Operation(
            summary = "Adds users to project.",
            description = """
                    Takes a project id and a list of user employee numbers and adds the users to the project.
                    Filters the user' labels optimizes the 'SYSTEM' scope labels in the project.
                    Whenever an employee number, which is non-existent in the DB is passed, the user is then extracted from the active directory and added to the project.
                    
                    Method is used in the Project Details view - by clicking the 'Add' button above the members table the user is shown a modal where he could search for users in the DB (or AD) and add them to the project.
                    Accessible only for 'ADMIN' or 'MANAGER' users.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Project updated.")
    void addUsers(@Parameter(name = "id", example = "1")long id, @Parameter(schema = @Schema(name = "employeeNumbers", example = """
            [
                "BB123456", "BB654321"
            ]
            """)) List<String> employeeNumbers);

    @Operation(
            summary = "Removes user from project.",
            description = """
                    Takes a project id and a user employee number and removes the user from the project.
                    Upon removing, the project's 'SYSTEM' scoped labels are optimized, removing any labels that none other project members hold.
                    
                    Method is used in the Project Details view - by clicking the 'Remove' button in the table, the manager or admin has the ability to remove said user from the project.
                    Accessible only for 'ADMIN' or 'MANAGER' users.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project/User not found.", content = @Content)
    @ApiResponse(responseCode = "204", description = "User removed.")
    void removeUser(@Parameter(name = "id", example = "1") long projectId, @RequestBody RemoveUserFromProjectCommand removeUserFromProjectCommand);

    @Operation(
            summary = "Receives a list of users with absences in the given project.",
            description = """
                    Receives a list of users in the given project, including their absences for the given period of time.
                    Method has the option to filter the users by employee numbers and/or labels.
                    
                    Used for displaying absences and users in the Project Calendar view.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectMemberWithAbsences.class))))
    List<ProjectMemberWithAbsences> getUsersAbsencesByLabels(@Valid @RequestBody GetUsersAbsencesByLabelsCommand getUsersAbsencesByLabelsCommand);

    @Operation(
            summary = "Receives the given user's capacities for each project they are included in.",
            description = """
                    Receives the given user's capacities for each project they are included in.
                    
                    Used in the Profile View to display all capacities (%) across the projects he is included in.
                    Accessible only for the logged user and 'MANAGER' or 'ADMIN' users.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Project not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users received.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectMemberWithCapacities.class)))
    ProjectMemberWithCapacities getProjectMemberWithCapacities(@Parameter(name = "employeeNumber", example = "BB123456") String employeeNumber);

    @Operation(
            summary = "Receives user's projects by user employee number.",
            description = """
                    Receives user's projects by user employee number.
                    
                    Used in the Project Calendar dropdown menu to display projects the logged user is included in.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Projects received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectName.class))))
    List<ProjectName> getProjectsByEmployeeId(@Parameter(name = "employeeId", example = "BB123456") String employeeId);
}
