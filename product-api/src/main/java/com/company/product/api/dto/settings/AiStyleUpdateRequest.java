package com.company.product.api.dto.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiStyleUpdateRequest(
    @NotBlank(message = "Название стиля обязательно")
    String styleName,
    @NotBlank(message = "Инструкция по стилю обязательна")
    @Size(max = 500, message = "Инструкция по стилю должна быть короче 500 символов")
    String styleInstruction
) {
}
