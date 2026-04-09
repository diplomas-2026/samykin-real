package com.company.product.api.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiAssistantMessageRequest(
    @NotBlank(message = "Сообщение обязательно")
    @Size(max = 2000, message = "Сообщение слишком длинное")
    String message,
    @Size(max = 12, message = "Слишком длинная история диалога")
    java.util.List<AiChatTurnRequest> history
) {
}
