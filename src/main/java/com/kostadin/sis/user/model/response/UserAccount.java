package com.kostadin.sis.user.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.user.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class UserAccount {
    @JsonIgnore
    private long id;
    @Schema(name = "firstName", example = "John", minLength = 2, maxLength = 255)
    private String firstName;
    @Schema(name = "lastName", example = "Doe", minLength = 2, maxLength = 255)
    private String lastName;
    @Schema(name = "employeeNumber", example = "BB123456", pattern = "EMP\\d{5}$")
    private String employeeNumber;
    @Schema(name = "phone", example = "0888888888", minLength = 10, maxLength = 10, pattern = "^0[89]\\d{8}$")
    private String phone;
    @Schema(name = "secondaryPhone", example = "0877777777", minLength = 10, maxLength = 10, pattern = "^0[89]\\d{8}$")
    private String secondaryPhone;
    @Schema(name = "email", example = "john.doe@email.com")
    private String email;
    @Schema(name = "secondaryEmail", example = "john.doe@email.com")
    private String secondaryEmail;
    @Schema(name = "company", example = "Company LTD", minLength = 2, maxLength = 255)
    private String company;
    @Schema(name = "role", example = "ROLE_EMPLOYEE")
    private UserRole role;
    @Schema(name = "labels", example = """
            [
                {
                    "id": 1,
                    "name": "Software Developer",
                    "scope": "SYSTEM"
                }
            ]
            """)
    private Set<LabelDTO> labels;
    @Schema(name = "color", example = "#ffffff", minLength = 7, maxLength = 7)
    private String color;
    @Schema(name = "subscribedToEmployeeNumber", example = """
            [
                "BB654321", "BV112233"
            ]
            """)
    private List<String> subscribedToEmployeeNumbers;
}
