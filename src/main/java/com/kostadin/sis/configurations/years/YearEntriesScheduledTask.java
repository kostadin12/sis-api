package com.kostadin.sis.configurations.years;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class YearEntriesScheduledTask {
    private static final String TASK_NAME = "import-next-year-working-days";

    private final YearEntriesService yearEntriesService;

    @Scheduled(cron = "0 0 18 * * *")
    @SchedulerLock(name = TASK_NAME)
    public void importUpcomingYear(){
        yearEntriesService.updateYearEntries();
        yearEntriesService.deleteYearsOlderThan(LocalDate.now().minusYears(3).getYear());
    }
}