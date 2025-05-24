package com.kostadin.sis.project.model.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AssignRemoveLabelNamesRequest {
    private List<String> labelNames;
}
