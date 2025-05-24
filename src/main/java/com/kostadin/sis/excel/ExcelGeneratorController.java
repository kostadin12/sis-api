package com.kostadin.sis.excel;

import com.kostadin.sis.excel.request.ExportToExcelCommand;
import com.kostadin.sis.excel.request.UserExportCommand;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sis/projects/v1.0.0")
public class ExcelGeneratorController implements ExcelGeneratorOperations{
    private final ExcelGeneratorService excelGeneratorService;

    @Override
    @GetMapping("/project-members")
    public void exportProjectMembers(HttpServletResponse response, @RequestParam long projectId) throws IOException {
        excelGeneratorService.exportProjectMembers(response, projectId);
    }

    @Override
    @PostMapping("/project-report")
    public void exportProjectMembersAbsences(HttpServletResponse response, @Valid @RequestBody ExportToExcelCommand exportToExcelCommand) throws IOException {
        excelGeneratorService.exportProjectMembersAbsences(response, exportToExcelCommand);
    }

    @Override
    @PostMapping("/users-report")
    public void exportUserReport(HttpServletResponse response, @Valid @RequestBody UserExportCommand userExportCommand) throws IOException {
        excelGeneratorService.exportUserReport(response, userExportCommand);
    }
}
