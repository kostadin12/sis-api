package com.kostadin.sis.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Not used when EmergencyCorsFilter is active, but kept for reference.
 */
@Configuration
class CorsConfig implements WebMvcConfigurer {
    // EmergencyCorsFilter takes precedence, leaving this empty
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     // CORS configuration handled by EmergencyCorsFilter
    // }
}