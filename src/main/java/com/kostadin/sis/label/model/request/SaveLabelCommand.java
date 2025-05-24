package com.kostadin.sis.label.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SaveLabelCommand {
    @Schema(name = "name", example = "Back-end Developer")
    @NotBlank(message = "Label name is mandatory.")
    @Length(max = 255, message = "Label name max length is 255.")
    private String name;
}
