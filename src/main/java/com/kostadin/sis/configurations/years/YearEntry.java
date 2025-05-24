package com.kostadin.sis.configurations.years;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "YEAR_ENTRIES")
public class YearEntry {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "YEAR_ENTRY")
    private String year;

    @Column(name = "NON_WORKING_DAYS")
    private String nonWorkingDays;
}
