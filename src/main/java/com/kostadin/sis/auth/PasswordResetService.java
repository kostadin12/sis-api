package com.kostadin.sis.auth;

import com.kostadin.sis.auth.model.PasswordResetToken;
import com.kostadin.sis.auth.repository.PasswordResetTokenRepository;
import com.kostadin.sis.common.exception.UserNotFoundException;
import com.kostadin.sis.common.exception.UserBadRequestException;
import com.kostadin.sis.config.mail.EmailSenderService;
import com.kostadin.sis.config.mail.EmailRequestBody;
import com.kostadin.sis.config.mail.template.EmailTemplate;
import com.kostadin.sis.config.mail.template.EmailTemplateRepository;
import com.kostadin.sis.config.mail.template.TemplateId;
import com.kostadin.sis.config.security.RateLimitingConfig;
import com.kostadin.sis.user.UserRepository;
import com.kostadin.sis.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;
    private final EmailTemplateRepository emailTemplateRepository;
    private final RateLimitingConfig rateLimitingConfig;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@company.com}")
    private String defaultFromEmail;

    /**
     * Generates a password reset token for the given email and sends email
     * @param email User's email address
     * @return Reset token
     */
    public String generatePasswordResetToken(String email) {
        log.info("Generating password reset token for email: {}", email);

        // Check rate limiting
        if (rateLimitingConfig.isRateLimitingEnabled() && isRateLimited(email)) {
            throw new UserBadRequestException("Too many password reset requests. Please try again later.");
        }

        // Check if user exists (but don't reveal this in the response)
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        // Generate token regardless of whether user exists (security through obscurity)
        String token = generateSecureToken();

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Invalidate any existing tokens for this email
            tokenRepository.invalidateAllTokensForEmail(email);

            // Create and save new token
            PasswordResetToken resetToken = new PasswordResetToken(
                    token,
                    email,
                    LocalDateTime.now().plusMinutes(rateLimitingConfig.getTokenExpirationMinutes())
            );

            tokenRepository.save(resetToken);

            // Send email asynchronously
            sendPasswordResetEmailAsync(user, token);

            log.info("Password reset token generated and email sent successfully for user: {}", email);
        } else {
            log.info("Password reset requested for non-existent email: {}", email);
            // Still log as if we sent an email for security
        }

        return token;
    }

    /**
     * Check if the email is rate limited
     */
    private boolean isRateLimited(String email) {
        LocalDateTime windowStart = LocalDateTime.now()
                .minusMinutes(rateLimitingConfig.getRateLimitWindowMinutes());

        long recentRequests = tokenRepository.countRecentRequestsByEmail(email, windowStart);

        return recentRequests >= rateLimitingConfig.getMaxRequestsPerEmail();
    }

    /**
     * Sends password reset email asynchronously
     */
    @Async
    public void sendPasswordResetEmailAsync(User user, String token) {
        sendPasswordResetEmail(user, token);
    }

    /**
     * Sends password reset email to the user
     * @param user User object
     * @param token Reset token
     */
    private void sendPasswordResetEmail(User user, String token) {
        try {
            // Get email template from database
            EmailTemplate template = emailTemplateRepository
                    .findByTemplateId(TemplateId.PASSWORD_RESET_REQUEST_EMAIL.name())
                    .orElseThrow(() -> new RuntimeException("Password reset email template not found in database"));

            // Create reset URL
            String resetUrl = frontendUrl + "/reset-password/" + token;

            // Replace placeholders in email template
            String emailBody = template.getBody()
                    .replace("{{USER_FIRST_NAME}}", user.getFirstName())
                    .replace("{{USER_LAST_NAME}}", user.getLastName())
                    .replace("{{RESET_URL}}", resetUrl)
                    .replace("{{EMPLOYEE_NUMBER}}", user.getEmployeeNumber())
                    .replace("{{EXPIRATION_MINUTES}}", String.valueOf(rateLimitingConfig.getTokenExpirationMinutes()));

            String emailSubject = template.getSubject()
                    .replace("{{USER_FIRST_NAME}}", user.getFirstName());

            // Create email request
            EmailRequestBody emailRequest = new EmailRequestBody()
                    .setSendTo(user.getEmail())
                    .setSentFrom(template.getSentFrom() != null ? template.getSentFrom() : defaultFromEmail)
                    .setReplyTo(template.getReplyTo())
                    .setSubject(emailSubject)
                    .setBodyContentType(template.getContentType() != null ? template.getContentType() : "text/html")
                    .setBody(emailBody);

            // Send email
            emailSenderService.sendEmail(emailRequest);

            log.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Sends password reset success email asynchronously
     */
    @Async
    public void sendPasswordResetSuccessEmailAsync(User user) {
        sendPasswordResetSuccessEmail(user);
    }

    /**
     * Sends password reset success email to the user
     * @param user User object
     */
    private void sendPasswordResetSuccessEmail(User user) {
        try {
            // Get email template from database
            EmailTemplate template = emailTemplateRepository
                    .findByTemplateId(TemplateId.PASSWORD_RESET_SUCCESS_EMAIL.name())
                    .orElseThrow(() -> new RuntimeException("Password reset success email template not found in database"));

            // Format the reset time
            String resetTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));

            // Replace placeholders in email template
            String emailBody = template.getBody()
                    .replace("{{USER_FIRST_NAME}}", user.getFirstName())
                    .replace("{{USER_LAST_NAME}}", user.getLastName())
                    .replace("{{EMPLOYEE_NUMBER}}", user.getEmployeeNumber())
                    .replace("{{RESET_TIME}}", resetTime);

            String emailSubject = template.getSubject()
                    .replace("{{USER_FIRST_NAME}}", user.getFirstName());

            // Create email request
            EmailRequestBody emailRequest = new EmailRequestBody()
                    .setSendTo(user.getEmail())
                    .setSentFrom(template.getSentFrom() != null ? template.getSentFrom() : defaultFromEmail)
                    .setReplyTo(template.getReplyTo())
                    .setSubject(emailSubject)
                    .setBodyContentType(template.getContentType() != null ? template.getContentType() : "text/html")
                    .setBody(emailBody);

            // Send email
            emailSenderService.sendEmail(emailRequest);

            log.info("Password reset success email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password reset success email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Resets password using the provided token
     * @param token Reset token
     * @param newPassword New password
     */
    public void resetPassword(String token, String newPassword) {
        log.info("Attempting to reset password with token: {}", token);

        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new UserBadRequestException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new UserBadRequestException("Reset token has expired");
        }

        User user = userRepository.findByEmailIgnoreCase(resetToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        tokenRepository.markTokenAsUsed(token);

        // Send success email asynchronously
        sendPasswordResetSuccessEmailAsync(user);

        log.info("Password reset successfully for user: {}", resetToken.getEmail());
    }

    /**
     * Validates if a reset token is valid and not expired
     * @param token Reset token to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidResetToken(String token) {
        return tokenRepository.findByTokenAndUsedFalse(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }

    /**
     * Generates a secure random token
     * @return Secure token string
     */
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }

        return token.toString();
    }

    /**
     * Cleanup expired tokens - runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredTokens() {
        try {
            LocalDateTime cutoff = LocalDateTime.now()
                    .minusDays(rateLimitingConfig.getTokenCleanupDays());

            tokenRepository.deleteExpiredTokens(LocalDateTime.now(), cutoff);

            log.debug("Cleaned up expired password reset tokens");
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens: {}", e.getMessage(), e);
        }
    }
}