package com.kostadin.sis.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class SubscribeToCommand {
    @Schema(name = "userEmployeeNumber", example = "BB123456")
    @NotBlank(message = "Please provide employee number.")
    private String userEmployeeNumber;
    @Schema(name = "subscribeToUserEmployeeNumbers", example = """
            [
                "BB123456",
                "BB654321"
            ]
            """)
    @NotEmpty(message = "Please provide employee numbers to subscribe to.")
    private List<String> subscribeToUserEmployeeNumbers;
}
