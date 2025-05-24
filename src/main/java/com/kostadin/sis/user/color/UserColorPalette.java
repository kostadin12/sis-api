package com.kostadin.sis.user.color;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record UserColorPalette(
        @Schema(name = "colors", example = """
                [ "#ffffff", "#000000" ]
                """)
        List<String> colors
) {
}
