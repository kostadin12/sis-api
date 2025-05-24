package com.kostadin.sis.userproject.model;

import com.kostadin.sis.project.model.response.ProjectNameWithCapacity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProjectMemberWithCapacities(
        @Schema(name = "employeeNumber", example = "BB123456")
        String employeeNumber,
        @Schema(name = "capacities", example = """
                [
                        {
                                "id" : 1,
                                "name" : "Teams Information System",
                                "capacityMode" : true
                                "capacity" : 80
                        }
                ]
                """)
        List<ProjectNameWithCapacity> capacities
        ) {
}
