package com.kostadin.sis.auth;

import com.kostadin.sis.auth.response.JwtResponse;
import com.kostadin.sis.auth.response.LoginRequest;
import com.kostadin.sis.auth.response.MessageResponse;
import com.kostadin.sis.auth.response.SignupRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authorization operations", description = """
        Endpoints for user authentication and registration:
        1. Login with email and password to get a JWT token.
        2. Register a new user in the system.
        """)
@RestController
@RequiredArgsConstructor
@RequestMapping("sis/authorization/v1.0.0")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authorizationService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok(authorizationService.registerUser(signUpRequest));
    }
}