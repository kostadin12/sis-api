package com.kostadin.sis.user.model.request;

import com.kostadin.sis.user.model.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class SaveUserCommand {
    @NotBlank(message = "First name is mandatory.")
    @Size(min = 2, max = 255,message = "User first name max length is 255.")
    private String firstName;

    @NotBlank(message = "Last name is mandatory.")
    @Size(min = 2, max = 255,message = "User last name max length is 255.")
    private String lastName;

    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    @NotBlank(message = "Employee number is mandatory.")
    private String employeeNumber;

    @NotBlank(message = "Phone number is mandatory.")
    @Pattern(regexp = "^0[89]\\d{8}$", message = "Invalid phone number.")
    @Size(min=10, max=10, message = "Phone number size is 10 digits.")
    private String phone;

    @Pattern(regexp = "^0[89]\\d{8}$", message = "Invalid phone number.")
    @Size(min=10, max=10, message = "Phone number size is 10 digits.")
    private String secondaryPhone;

    @NotBlank(message = "Email is mandatory.")
    @Email(message = "Invalid email.")
    @Size(max = 255,message = "User email max length is 255.")
    private String email;

    @Email(message = "Invalid email.")
    @Size(max = 255,message = "User email max length is 255.")
    private String secondaryEmail;

    @NotBlank(message = "Company name is mandatory.")
    @Size(max = 255,message = "User email max length is 255.")
    private String company;

    @NotNull(message = "User role is mandatory.")
    private UserRole role;

    @NotBlank(message = "Color is mandatory")
    @Length(min = 7, max = 7, message = "Color hex code must be 7 characters long.")
    private String color;

    private List<Long> labels;
}
