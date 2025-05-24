package com.kostadin.sis.absence.controller;

import com.kostadin.sis.absence.AbsenceService;
import com.kostadin.sis.absence.model.request.SaveAbsenceCommand;
import com.kostadin.sis.absence.model.response.AbsenceDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/absences/v1.0.0")
public class AbsenceController implements AbsenceOperations{

    private final AbsenceService absenceService;

    @Override
    @PostMapping
    @ResponseStatus(CREATED)
    public AbsenceDTO saveAbsence(@Valid @RequestBody SaveAbsenceCommand saveAbsenceCommand) {
        return absenceService.saveAbsence(saveAbsenceCommand);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AbsenceDTO> getAbsenceById(@PathVariable("id") long id) {
        return ResponseEntity.ok(absenceService.getAbsenceById(id));
    }

    @Override
    @PatchMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    public void updateAbsence(@PathVariable("id") long id, @Valid @RequestBody SaveAbsenceCommand saveAbsenceCommand) {
        absenceService.updateAbsence(id, saveAbsenceCommand);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteAbsence(@PathVariable("id") long id, @RequestParam String employeeNumber) {
        absenceService.deleteAbsence(id, employeeNumber);
    }
}
