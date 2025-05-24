package com.kostadin.sis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties("user")
public class UserProperties {
    private UserColorConfig color;

    @Data
    public static class UserColorConfig {
        private int tolerance;
        private Set<String> ignoredColors;
    }
}
