package com.kostadin.sis.userproject.model;

import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.user.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;

import java.util.Objects;
import java.util.Set;

public record ProjectMember(
        @Schema(name = "firstName", example = "John")
        String firstName,
        @Schema(name = "lastName", example = "Doe")
        String lastName,
        @Schema(name = "employeeNumber", example = "BB123456")
        String employeeNumber,
        @Schema(name = "phone", example = "0899999999")
        String phone,
        @Schema(name = "secondaryPhone", example = "0988888888")
        String secondaryPhone,
        @Schema(name = "email", example = "john.doe@email.com")
        String email,
        @Schema(name = "secondaryEmail", example = "john.doe@email.com")
        String secondaryEmail,
        @Schema(name = "company", example = "Company LTD")
        String company,
        @Schema(name = "color", example = "#ffffff")
        String color,
        UserRole role,
        @Schema(name = "labels", example = """
                [
                    {
                      "id": 1,
                      "name": "Software Developer",
                      "scope": "SYSTEM"
                    },
                    {
                      "id": 55,
                      "name": "Back-end Developer",
                      "scope": "PROJECT"
                    }
                ]
                """)
        @With Set<LabelDTO> labels,
        @Schema(name = "capacity", example = "80")
        int capacity) {

    public ProjectMember {
        labels = Objects.requireNonNullElseGet(labels, Set::of);
    }
}
