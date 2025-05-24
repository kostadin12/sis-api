package com.kostadin.sis.auth;

import com.kostadin.sis.account.UserAccountService;
import com.kostadin.sis.auth.response.JwtResponse;
import com.kostadin.sis.auth.response.LoginRequest;
import com.kostadin.sis.auth.response.MessageResponse;
import com.kostadin.sis.auth.response.SignupRequest;
import com.kostadin.sis.auth.security.jwt.JwtUtils;
import com.kostadin.sis.auth.security.services.UserDetailsImpl;
import com.kostadin.sis.common.exception.UserBadRequestException;
import com.kostadin.sis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserAccountService userAccountService;
    private final Random random = new Random();

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .employeeNumber(userDetails.getEmployeeNumber())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }

    public MessageResponse registerUser(SignupRequest signUpRequest) {
        // Check if email exists
        if (userRepository.existsByEmailIgnoreCase(signUpRequest.getEmail())) {
            throw new UserBadRequestException("Error: Email is already taken!");
        }

        // Generate unique employee number if not provided or already exists
        if (signUpRequest.getEmployeeNumber() == null || signUpRequest.getEmployeeNumber().isEmpty()
                || userRepository.existsByEmployeeNumber(signUpRequest.getEmployeeNumber())) {

            // If the provided employee number exists, we'll generate a new one
            if (signUpRequest.getEmployeeNumber() != null && !signUpRequest.getEmployeeNumber().isEmpty()
                    && userRepository.existsByEmployeeNumber(signUpRequest.getEmployeeNumber())) {
                log.info("Employee number {} already exists. Generating a new unique employee number.",
                        signUpRequest.getEmployeeNumber());
            }

            // Generate a unique employee number
            String employeeNumber = generateUniqueEmployeeNumber();
            signUpRequest.setEmployeeNumber(employeeNumber);

            log.info("Generated unique employee number: {}", employeeNumber);
        } else {
            // Validate the format of the provided employee number
            if (!isValidEmployeeNumberFormat(signUpRequest.getEmployeeNumber())) {
                throw new UserBadRequestException("Error: Employee number must follow the format EMP12345 where 12345 is a 5-digit number.");
            }
        }

        // Use the userAccountService to create a new user
        userAccountService.createUserWithEmail(signUpRequest);

        return new MessageResponse("User registered successfully with employee number: " + signUpRequest.getEmployeeNumber());
    }

    /**
     * Generates a unique employee number in the format EMP12345
     * @return A unique employee number
     */
    private String generateUniqueEmployeeNumber() {
        String employeeNumber;
        int maxAttempts = 100; // Prevent infinite loop
        int attempts = 0;

        do {
            int randomNumber = 10000 + random.nextInt(90000);
            employeeNumber = "EMP" + randomNumber;
            attempts++;

            // Prevent infinite loop
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate a unique employee number after " + maxAttempts + " attempts.");
            }

        } while (userRepository.existsByEmployeeNumber(employeeNumber));

        return employeeNumber;
    }

    /**
     * Validates that the employee number follows the format EMP12345
     * @param employeeNumber The employee number to validate
     * @return true if the format is valid, false otherwise
     */
    private boolean isValidEmployeeNumberFormat(String employeeNumber) {
        return employeeNumber != null && employeeNumber.matches("^EMP\\d{5}$");
    }
}