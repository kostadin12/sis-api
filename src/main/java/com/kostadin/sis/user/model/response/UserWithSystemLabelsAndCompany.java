package com.kostadin.sis.user.model.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserWithSystemLabelsAndCompany(
        @Schema(name = "firstName", example = "John")
        String firstName,
        @Schema(name = "lastName", example = "Doe")
        String lastName,
        @Schema(name = "employeeNumber", example = "BB123456")
        String employeeNumber,
        @Schema(name = "company", example = "Company LTD")
        String company,
        @Schema(name = "systemLabels", example = """
                [
                    "Software Developer"
                ]
                """)
        List<String> systemLabels
) {
}
