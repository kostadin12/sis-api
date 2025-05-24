package com.kostadin.sis.auth;

import com.kostadin.sis.auth.response.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Password Reset", description = "Endpoints for password reset functionality")
@RestController
@RequestMapping("/sis/authorization/password-reset")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "Request password reset",
            description = "Sends a password reset token for the provided email address")
    @PostMapping("/request")
    public ResponseEntity<MessageResponse> requestPasswordReset(@RequestBody ForgotPasswordRequest request) {

        passwordResetService.generatePasswordResetToken(request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
                "If an account with that email exists, a password reset link has been sent."
        ));
    }

    @Operation(summary = "Validate reset token",
            description = "Checks if a password reset token is valid and not expired")
    @GetMapping("/validate/{token}")
    public ResponseEntity<MessageResponse> validateResetToken(@PathVariable String token) {

        boolean isValid = passwordResetService.isValidResetToken(token);

        if (isValid) {
            return ResponseEntity.ok(new MessageResponse("Reset token is valid"));
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid or expired reset token"));
        }
    }

    @Operation(summary = "Reset password",
            description = "Resets the password using a valid reset token")
    @PostMapping("/reset")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }

    @Data
    public static class ForgotPasswordRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "Reset token is required")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
        private String newPassword;
    }
}