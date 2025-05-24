package com.kostadin.sis.subscriptions.controller;

import com.kostadin.sis.user.model.request.SubscribeToCommand;
import com.kostadin.sis.user.model.request.UnsubscribeFromCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Subscription operations",description = """
        Endpoints that allow:
        1. Subscribing to a bunch of users by given employee numbers.
        2. Unsubscribing from a bunch of users by given employee numbers.
        """)
public interface SubscriptionOperations {
    @Operation(
            summary = "Subscribes the user to another user.",
            description = """
                    Subscribes the user to another user. Subscriptions are used whenever new absences are created - the absent user's subscribers would receive an Outlook appointment, as a notification for the newly created absence.
                    
                    Method is used in the Project Details view, where all users could subscribe to other project participants by clicking the bell icon in the table.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Subscription successful.")
    void subscribeTo(@RequestBody SubscribeToCommand subscribeToCommand);

    @Operation(
            summary = "Unsubscribes the user from another user.",
            description = """
                    Unsubscribes the user from another user. Unsubscribing ensures the user is no longer receiving Outlook notifications for newly created absences.
                    
                    Method is used in the Project Details view, where all users could unsubscribe from other project participants by clicking the bell icon in the table.
                    """
    )
    @ApiResponse(responseCode = "401", description = "Unauthorized request.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden request.", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found.", content = @Content)
    @ApiResponse(responseCode = "202", description = "Unsubscribed successfully.")
    void unsubscribeFrom(@RequestBody UnsubscribeFromCommand unsubscribeFromCommand);
}
