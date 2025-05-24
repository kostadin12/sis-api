package com.kostadin.sis.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FilterUsersForReportCommand {
    @Schema(name = "company", example = "Company LTD")
    private String company;
    @Schema(name = "systemLabel", example = "Software Developer")
    private String systemLabel;
}
