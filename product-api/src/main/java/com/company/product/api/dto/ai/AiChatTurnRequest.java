package com.company.product.api.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiChatTurnRequest(
    @Pattern(regexp = "user|assistant", message = "Недопустимая роль сообщения")
    String role,
    @NotBlank(message = "Текст сообщения обязателен")
    @Size(max = 2000, message = "Текст сообщения слишком длинный")
    String content
) {
}
