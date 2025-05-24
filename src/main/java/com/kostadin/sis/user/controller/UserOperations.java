package com.kostadin.sis.user.controller;

import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.user.model.request.AssignRemoveProjectLabelCommand;
import com.kostadin.sis.user.model.request.AssignRemoveSystemLabelCommand;
import com.kostadin.sis.user.model.request.FilterUsersForReportCommand;
import com.kostadin.sis.user.model.request.UpdateUserCommand;
import com.kostadin.sis.user.model.response.UserDTO;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import com.kostadin.sis.user.model.response.UserWithSystemLabelsAndCompany;
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
import java.util.Set;

@Tag(name = "User operations",description = """
        Endpoints that allow:
        1. New User entities to be created, received, updated or deleted from/to the database.
        2. Assign a SYSTEM scope label to a User.
        3. Assign a PROJECT scope label to a User in a Project.
        4. Remove a label from a User.
        5. Receive all User's projects.
        6. Receive all User absences.
        7. List all Users by searching for their name/employeeNumber (autocomplete feature).
        """)
public interface UserOperations {
    @Operation(
            summary = "[UNUSED] Receives a list of all users in the DB.",
            description = """
                    [UNUSED]
                    Receives a list of all users in the DB.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users received.",content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserNameEmployeeNumber.class))))
    List<UserNameEmployeeNumber> getAll();

    @Operation(
            summary = "Receives a user from the DB by employee number.",
            description = """
                    Receives a user from the DB by employee number.
                    
                    Method is used when displaying user information.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User received.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    UserDTO getUser(@Parameter(name = "employeeId", example = "BB123456") String employeeId);
    @Operation(
            summary = "Autocomplete search by name / employee number.",
            description = """
                    Method which returns users by applying the given filter.
                    Could be used in 2 ways:
                    1. As an autocomplete search by name / employee number.
                    If the given filter does not find any users in the DB, the search would continue and return results from the active directory.
                    3. As an autocomplete search by name / employee number for selecting a substitute (a user which participates in the same project(s) as the given absent user).
                    
                    Method is used for autocomplete searching when adding users to a project.
                    Also used when searching for an absence substitute.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserNameEmployeeNumber.class))))
    List<UserNameEmployeeNumber> findUsers(@Parameter(name = "filter", example = "John Doe") String filter, @Parameter(name = "absentUserEmployeeNumber", example = "BB123456") String absentUserEmployeeNumber);

    @Operation(
            summary = "Autocomplete search by name / employee number for project owner beneficiaries.",
            description = """
                    Retrieves a list beneficiary candidates for a project owner by a given filter (as an autocomplete filter).
                    
                    Used whenever a project owner is removed from the project, either by himself or an 'ADMIN' user, in order to find a beneficiary for the new project owner.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserNameEmployeeNumber.class))))
    List<UserNameEmployeeNumber> findBeneficiaryCandidates(@Parameter(name = "filter", example = "John Doe") String filter, @Parameter(name = "projectId", example = "1") long projectId);
    @Operation(
            summary = "Updates a user in the DB.",
            description = """
                    Updates a user in the DB. Method takes an UpdateUserCommand body, which contains the required user information.
                    Also has the option to update his 'SYSTEM' scope labels and his capacities between different projects.
                    
                    Method is used in the login view, whenever a newly created user is returned an has to be updated, in order for the new user to confirm his account creation.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invalid user.", content = @Content)
    @ApiResponse(responseCode = "202", description = "User updated.")
    void updateUser(@Parameter(name = "employeeId", example = "BB123456") String employeeId, @RequestBody UpdateUserCommand updateUserCommand);
    @Operation(
            summary = "[UNUSED] Deletes a user from the DB by employee number.",
            description = """
                    [UNUSED]
                    Deletes a user from the DB by employee number.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "204", description = "User deleted.")
    void deleteUser(@Parameter(name = "employeeId", example = "BB123456") String employeeId);

    @Operation(
            summary = "Receives user's absences by user employee number.",
            description = """
                    Receives user's absences by user employee number.
                    
                    Used in the Personal Calendar view to display the logged user's absences.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Absences received.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AbsenceDTO.class))))
    List<AbsenceDTO> getAbsencesByEmployeeId(@Parameter(name = "employeeId", example = "BB123456") String employeeId);

    @Operation(
            summary = "Assigns a 'PROJECT' scoped label to a user in the given project.",
            description = """
                    Assigns a 'PROJECT' scoped label to a user in the given project.
                    Method validates that the given label has the correct scope and is included in the given project.
                    Then it adds it to the user's label collection.
                    
                    Method is used in the Project Details view - by clicking on the label field in the members table, a modal is shown where a manager or admin has the option to assign labels to a user.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User/Project/Label not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User updated.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    ResponseEntity<UserDTO> assignProjectLabel(@RequestBody AssignRemoveProjectLabelCommand requestBody);

    @Operation(
            summary = "[UNUSED] Assigns a 'SYSTEM' scoped label to a user by employee number.",
            description = """
                    [UNUSED]
                    Assigns a 'SYSTEM' scoped label to a user by employee number.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User/Label not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User updated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    ResponseEntity<UserDTO> assignSystemLabel(@RequestBody AssignRemoveSystemLabelCommand requestBody);

    @Operation(
            summary = "Removes a 'PROJECT' scoped label from a user.",
            description = """
                    Removes a 'PROJECT' scoped label from a user.
                    The method confirms the project and user contain the given label and remove it from the user's label collection.
                    
                    Method is used in the Project Details view - by clicking on the label field in the members table, a modal is shown where a manager or admin has the option to remove labels from a user.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User/Label not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User updated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    ResponseEntity<UserDTO> removeProjectLabel(@RequestBody AssignRemoveProjectLabelCommand requestBody);

    @Operation(
            summary = "[UNUSED] Removes a 'SYSTEM' scope label from a user.",
            description = """
                    [UNUSED]
                    Removes a 'SYSTEM' scope label from a user.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User/Label not found.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User updated.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    ResponseEntity<UserDTO> removeSystemLabel(@RequestBody AssignRemoveSystemLabelCommand requestBody);

    @Operation(
            summary = "Returns all companies from the DB.",
            description = """
                    Returns all companies from the DB.
                    
                    Method is used when doing a user report, an filtering by companies.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Companies returned.", content = @Content(schema = @Schema(example = """
            [
                "Company LTD", "Another Company AD"
            ]
            """)))
    Set<String> getAllCompanies();

    @Operation(
            summary = "Returns users filtered by companies or/and system labels.",
            description = """
                    Returns users filtered by companies or/and system labels.
                    
                    Method is used when doing a user report - for filtering users in DB.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Users returned.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserWithSystemLabelsAndCompany.class))))
    Set<UserWithSystemLabelsAndCompany> filterUsersByCompaniesAndSystemLabels(@RequestBody FilterUsersForReportCommand filterUsersForReportCommand);
}
