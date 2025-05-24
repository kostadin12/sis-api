package com.kostadin.sis.user.color;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserColorResponse (
        @Schema(name = "color", description = "Randomized hex color response.", example = "#ffffff")
        String color
){
}
