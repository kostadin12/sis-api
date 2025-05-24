package com.kostadin.sis.project.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class CreateProjectLabelCommand {
    @NotEmpty(message = "Label names field is empty.")
    private List<String> labelNames;
}
