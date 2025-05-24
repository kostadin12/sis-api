package com.kostadin.sis.excel.request;

import com.kostadin.sis.project.model.Project;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class ExportToExcelVariables {
    Project project;
    LocalDate startDate;
    LocalDate endDate;
    int workingDaysInPeriod;
    List<String> labels;
    List<UserExcelDTO> users;
    UserExcelDTO user;
    int periodCapacityCap;
}
