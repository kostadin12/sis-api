package com.kostadin.sis.label.controller;

import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.label.LabelService;
import com.kostadin.sis.label.model.LabelScope;
import com.kostadin.sis.label.model.request.SaveLabelCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/labels/v1.0.0")
public class    LabelController implements LabelOperations{
    private final LabelService labelService;

    @Override
    @PostMapping
    @ResponseStatus(CREATED)
    @Deprecated(forRemoval = true)
    public void saveLabel(@Valid @RequestBody SaveLabelCommand saveLabelCommand) {
        labelService.saveLabel(saveLabelCommand);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<LabelDTO>> getAll(@RequestParam LabelScope labelScope) {
        return ResponseEntity.ok(labelService.getLabels(labelScope));
    }

    @Override
    @GetMapping("/{id}")
    @Deprecated(forRemoval = true)
    public ResponseEntity<LabelDTO> getLabel(@PathVariable("id") long id) {
        return ResponseEntity.ok(labelService.getLabel(id));
    }

    @Override
    @PatchMapping("/{id}")
    @ResponseStatus(ACCEPTED)
    @Deprecated(forRemoval = true)
    public void updateLabel(@PathVariable("id") long id, @Valid @RequestBody SaveLabelCommand saveLabelCommand) {
        labelService.updateLabel(id,saveLabelCommand);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    @Deprecated(forRemoval = true)
    public void deleteLabel(@PathVariable("id") long id) {
        labelService.deleteLabel(id);
    }

    @Override
    @GetMapping("/find")
    public List<LabelDTO> findLabelsByScopeAndName(@RequestParam LabelScope labelScope, @RequestParam String filter) {
        return labelService.findLabelsByScopeAndName(labelScope, filter);
    }
}
