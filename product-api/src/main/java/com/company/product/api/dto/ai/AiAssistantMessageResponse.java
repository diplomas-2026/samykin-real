package com.company.product.api.dto.ai;

public record AiAssistantMessageResponse(
    String reply,
    int usedTokens,
    int remainingTokens
) {
}
