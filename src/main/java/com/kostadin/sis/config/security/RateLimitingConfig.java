package com.kostadin.sis.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.password-reset")
@Getter
@Setter
public class RateLimitingConfig {

    /**
     * Maximum number of password reset requests per email within the time window
     */
    private int maxRequestsPerEmail = 3;

    /**
     * Time window for rate limiting in minutes
     */
    private int rateLimitWindowMinutes = 15;

    /**
     * Token expiration time in minutes
     */
    private int tokenExpirationMinutes = 15;

    /**
     * Maximum age of tokens to keep in database (for cleanup) in days
     */
    private int tokenCleanupDays = 7;

    /**
     * Whether to enable rate limiting
     */
    private boolean rateLimitingEnabled = true;

    /**
     * Whether to send email even if user doesn't exist (security through obscurity)
     */
    private boolean alwaysSendResponse = true;
}