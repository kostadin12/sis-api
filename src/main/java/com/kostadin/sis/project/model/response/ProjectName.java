package com.kostadin.sis.project.model.response;


import io.swagger.v3.oas.annotations.media.Schema;

public record ProjectName (
        @Schema(name = "id", example = "1") long id,
        @Schema(name = "name", example = "Teams Information System") String name
) {
}
