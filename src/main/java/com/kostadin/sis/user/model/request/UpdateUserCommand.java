package com.kostadin.sis.user.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kostadin.sis.user.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateUserCommand {
    @Schema(name = "firstName", example = "John")
    @NotBlank(message = "First name is mandatory.")
    @Size(min = 2, max = 255,message = "User first name max length is 255.")
    private String firstName;

    @Schema(name = "lastName", example = "Doe")
    @NotBlank(message = "Last name is mandatory.")
    @Size(min = 2, max = 255,message = "User last name max length is 255.")
    private String lastName;

    @Schema(name = "phone", example = "0899999999")
    @Pattern(regexp = "^0[89]\\d{8}$", message = "Invalid phone number.")
    @Size(min=10, max=10, message = "Phone number size is 10 digits.")
    private String phone;

    @Schema(name = "secondaryPhone", example = "0988888888")
    @Pattern(regexp = "^0[89]\\d{8}$", message = "Invalid phone number.")
    @Size(min=10, max=10, message = "Phone number size is 10 digits.")
    private String secondaryPhone;

    @Schema(name = "email", example = "john.doe@email.com")
    @Email(message = "Invalid email.")
    @Size(max = 255,message = "User email max length is 255.")
    private String email;

    @Schema(name = "secondaryEmail", example = "john.doe@email.com")
    @Email(message = "Invalid email.")
    @Size(max = 255,message = "User email max length is 255.")
    private String secondaryEmail;

    @Schema(name = "color", example = "#ffffff")
    @Length(min = 7, max = 7, message = "Color hex code must be 7 characters long.")
    private String color;

    @Schema(name = "role", example = "ROLE_ADMIN")
    private UserRole role;

    @Schema(name = "labels", example = """
            [
                1,3,7
            ]
            """)
    private List<Long> labels;

    @Schema(name = "projectCapacity", example = """
            {
                "1": 60,
                "2": 40
            }
            """)
    private Map<Long, Integer> projectCapacity;

    @JsonIgnore
    public boolean isColorWhite(){
        return Objects.equals(this.color, "#ffffff");
    }
}
