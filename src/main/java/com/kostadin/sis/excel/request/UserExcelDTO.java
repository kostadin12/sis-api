package com.kostadin.sis.excel.request;

import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.user.model.UserRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class UserExcelDTO {
    String firstName;
    String lastName;
    String employeeNumber;
    String phone;
    String secondaryPhone;
    String email;
    String secondaryEmail;
    String company;
    UserRole role;
    Set<LabelDTO> labels;

    List<ProjectExcelDTO> projects;

    int totalWorkingDays;
    int daysOff;
    double daysAvailable;

    @Data
    @Accessors(chain = true)
    public static class ProjectExcelDTO {
        private String name;
        private List<String> labels;
        private int capacity;
        private boolean capacityMode;
    }
}
