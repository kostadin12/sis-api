package com.kostadin.sis.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI sisMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("IntDev Sis")
                        .description("""
                                Sis is a dynamic and user-friendly Spring Boot + React microservice
                                application designed to simplify and enhance absence tracking within
                                organizations. Sis offers a seamless and efficient solution for
                                managing individual and team absences.
                                """)
                        .version("1.0.0"));
    }
}