package com.kostadin.sis.absence.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kostadin.sis.absence.model.AbsenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Objects;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class SaveAbsenceCommand {
    @Schema(name = "startDate", description = "Absence start date", example = "2020-01-01")
    @NotNull(message = "Absence start date is mandatory.")
    private LocalDate startDate;

    @Schema(name = "endDate", description = "Absence end date", example = "2020-01-02")
    @NotNull(message = "Absence end date is mandatory.")
    private LocalDate endDate;

    @Schema(name = "absenceType", description = "Absence type", example = "SICK_LEAVE")
    @NotNull(message = "Absence type is mandatory.")
    private AbsenceType absenceType;

    @Schema(name = "employeeNumber", description = "Employee number of absent user", example = "BB123456", pattern = "^EMP\\d{5}$")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid employee number.")
    @NotBlank(message = "Employee number is mandatory.")
    private String employeeNumber;

    @Schema(name = "substituteEmployeeNumber", description = "Non required field - employee number of absence substitute.", example = "BB654321")
    @Pattern(regexp = "^EMP\\d{5}$", message = "Invalid substitute employee number.")
    private String substituteEmployeeNumber;

    @JsonIgnore
    public boolean isEmployeeSubstituteToHimself() {
        if (getEmployeeNumber() != null && getSubstituteEmployeeNumber() != null){
            return Objects.equals(getEmployeeNumber(),getSubstituteEmployeeNumber());
        }
        return false;
    }

    @JsonIgnore
    public boolean isAbsencePeriodTooLong() {
        return getStartDate().until(getEndDate(), DAYS) > 30;
    }

    @JsonIgnore
    public boolean isAbsenceTooFarBackOrTooFarAhead() {
        return (getStartDate().isBefore(now()) && getStartDate().until(now(), DAYS) > 365) || (getEndDate().isAfter(now()) && now().until(getEndDate(), DAYS) > 365);
    }

    @JsonIgnore
    public boolean isStartDateAfterEndDate() {
        if (getStartDate() != null && getEndDate() != null){
            return getStartDate().isAfter(getEndDate());
        }
        return false;
    }

}
