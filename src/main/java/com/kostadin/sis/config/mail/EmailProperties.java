package com.kostadin.sis.config.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("email")
public class EmailProperties {
    private String uri;

    private String clientId;
}
