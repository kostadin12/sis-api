package com.kostadin.sis.absence.model.response;

import com.kostadin.sis.absence.model.Absence;
import com.kostadin.sis.absence.model.AbsenceType;
import com.kostadin.sis.user.model.response.UserNameEmployeeNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AbsenceDTO {
    @Schema(name = "id", example = "1")
    private long id;
    @Schema(name = "startDate", example = "2020-01-01")
    private LocalDate startDate;
    @Schema(name = "endDate", example = "2020-01-02")
    private LocalDate endDate;
    @Schema(name = "bookedDate", example = "2020-01-01")
    private LocalDate bookedDate;
    @Schema(name = "absenceType", example = "SICK_LEAVE")
    private AbsenceType absenceType;
    @Schema(name = "substitute", example = """
            {
                "firstName" : "John",
                "lastName" : "Doe",
                "employeeNumber" : "BB123456"
            }
            """)
    private UserNameEmployeeNumber substitute;

    public static AbsenceDTO of(Absence absence) {
        return new AbsenceDTO()
                .setId(absence.getId())
                .setStartDate(absence.getStartDate())
                .setEndDate(absence.getEndDate())
                .setAbsenceType(absence.getAbsenceType())
                .setBookedDate(absence.getBookedDate())
                .setSubstitute(absence.getSubstitute() != null ? new UserNameEmployeeNumber(
                        absence.getSubstitute().getFirstName(),
                        absence.getSubstitute().getLastName(),
                        absence.getSubstitute().getEmployeeNumber()
                ) : null);
    }
}
