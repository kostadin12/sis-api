package com.kostadin.sis.configurations.years;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/year-entries/v1.0.0")
public class YearEntriesController implements YearEntriesOperations{
    private final YearEntriesService yearEntriesService;

    @Override
    @PostMapping("/")
    public List<String> getNonWorkingDays(@Valid @RequestBody ReceiveNonWorkingDaysCommand command) {
        return yearEntriesService.getNonWorkingDays(command);
    }
}
