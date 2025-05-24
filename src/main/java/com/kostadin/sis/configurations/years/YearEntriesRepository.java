package com.kostadin.sis.configurations.years;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearEntriesRepository extends JpaRepository<YearEntry, Long> {
    Optional<YearEntry> findByYear(String year);
}
