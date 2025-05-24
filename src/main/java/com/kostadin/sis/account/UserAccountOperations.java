package com.kostadin.sis.account;

import com.kostadin.sis.user.color.UserColorPalette;
import com.kostadin.sis.user.color.UserColorResponse;
import com.kostadin.sis.user.model.response.UserAccount;
import com.kostadin.sis.user.model.response.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User account",description = """
        Endpoint responsible for receiving the User account (if one is existent), or receiving the information for yet unregistered Users.
        """)
public interface UserAccountOperations {
    @Operation(
            summary = "Receives logged user.",
            description = """
                    Searches the DB for the authenticated user, and returns him if existent.
                    If not, a new user would be created in the DB and returned.
                    When creating a new user, the required information is extracted from the JWT, and then saved to the DB.
                    
                    Method is used whenever a user logs in Sis. If he exists in the DB, he would be already logged in. 
                    If he is a newly created user, he would be shown a login modal, where he would be shown the extracted information from the active directory and be able to change it and create his account.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "User account found in DB.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "201", description = "Created new user.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class)))
    ResponseEntity<UserAccount> getUserAccount(@Parameter(name = "email", example = "dohn.joe@gmail.com") String email);

    @Operation(
            summary = "Receives randomized hex color.",
            description = """
                    Receives a randomized hex color, which is unique from all user colors.
                    Before returning the randomized hex color, a method which calculates the euclidean distance of the generated color with every color from the DB. 
                    If the new color is unique and meets the requirements (the euclidean distance must be enough from the other colors), it is returned to the FE.
                    
                    Method is used in the login modal, for picking a new color for the newly created user. It is also used in the Profile View, where the user has the option to change his color.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Received random hex color.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserColorResponse.class)))
    UserColorResponse getRandomColor(@Parameter(name = "employeeNumber", example = "BB123456") String employeeNumber);

    @Operation(
            summary = "Receives randomized hex colors.",
            description = """
                    Receives a list of randomized hex color, which are unique from all user colors.
                    Before returning the randomized hex colors, a method which calculates the euclidean distance of the generated color with every color from the DB. 
                    If the new color is unique and meets the requirements (the euclidean distance must be enough from the other colors), it is returned to the FE.
                    
                    Method is used in the login modal, for picking a new color for the newly created user. It is also used in the Profile View, where the user has the option to change his color.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "200", description = "Received random hex colors.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserColorPalette.class)))
    UserColorPalette getColorPalette(@Parameter(name = "count", description = "Count of generated colors. Must be between 1 and 30.", example = "10") int count, @Parameter(name = "employeeNumber", example = "BB123456") String employeeNumber);
}
