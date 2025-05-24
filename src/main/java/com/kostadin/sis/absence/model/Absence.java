package com.kostadin.sis.absence.model;

import com.kostadin.sis.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "ABSENCES")
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull(message = "Absence start date is mandatory.")
    private LocalDate startDate;
    @NotNull(message = "Absence end date is mandatory.")
    private LocalDate endDate;

    private LocalDate bookedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ABSENCE_TYPE")
    @NotNull(message = "Absence type is mandatory.")
    private AbsenceType absenceType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SUBSTITUTE_ID")
    private User substitute;
}