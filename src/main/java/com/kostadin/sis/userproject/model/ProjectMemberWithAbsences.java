package com.kostadin.sis.userproject.model;

import com.kostadin.sis.absence.model.response.AbsenceDTO;
import com.kostadin.sis.label.model.response.LabelDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;

import java.util.List;
import java.util.Set;

public record ProjectMemberWithAbsences(
        @Schema(name = "firstName", example = "John")
        String firstName,
        @Schema(name = "lastName", example = "Doe")
        String lastName,
        @Schema(name = "employeeNumber", example = "BB123456")
        String employeeNumber,
        @Schema(name = "color", example = "#ffffff")
        String color,
        @Schema(name = "labels", example = """
            [
                {
                    "id": 1,
                    "name": "Software Developer",
                    "scope": "SYSTEM"
                }
            ]
                """)
        @With Set<LabelDTO> labels,
        @Schema(name = "absences", example = """
            [
                {
                    "id": 1,
                    "startDate": "2020-01-01",
                    "endDate": "2020-01-02",
                    "bookedDate": "2020-01-01",
                    "absenceType": "SICK_LEAVE",
                    "substituteEmployeeNumber": "BB654321"
                }
            ]
                """)

        @With List<AbsenceDTO> absences
) {

}
