package com.kostadin.sis.project.model.response;

import com.kostadin.sis.label.model.response.LabelDTO;
import com.kostadin.sis.project.model.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ProjectDTO {
    @Schema(name = "id", example = "1")
    private long id;
    @Schema(name = "name", example = "Teams Information System")
    private String name;
    @Schema(name = "description", example = "Project description.")
    private String description;
    @Schema(name = "startDate", example = "2020-01-01")
    private LocalDate startDate;
    @Schema(name = "endDate", example = "null")
    private LocalDate endDate;
    private ProjectStatus projectStatus;
    @Schema(name = "projectOwner", example = "BB123456")
    private String projectOwner;
    private boolean capacityMode;
    @Schema(name = "labels", example = """
            [
                {
                    "id": 1,
                    "name": "Software Developer",
                    "scope": "SYSTEM"
                }
            ]
            """)
    private Set<LabelDTO> labels;
}
