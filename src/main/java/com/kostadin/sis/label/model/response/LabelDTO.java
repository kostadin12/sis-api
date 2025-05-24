package com.kostadin.sis.label.model.response;

import com.kostadin.sis.label.model.Label;
import com.kostadin.sis.label.model.LabelScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LabelDTO {
    @Schema(name = "id", example = "1")
    private long id;
    @Schema(name = "name", example = "Software Developer")
    @NotBlank(message = "Label name is mandatory.")
    @Length(max = 255, message = "Label name max length is 255.")
    private String name;
    @Schema(name = "scope", example = "SYSTEM")
    @NotNull(message = "Label scope is mandatory.")
    private LabelScope scope;

    public static LabelDTO of(Label label) {
        return new LabelDTO()
                .setId(label.getId())
                .setName(label.getName())
                .setScope(label.getScope());
    }
}
