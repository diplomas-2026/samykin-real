package com.company.product.api.dto.ai;

import java.util.List;

public record AiAssistantMessageResponse(
    String message,
    List<AiAssistantPayoutResponse> payouts,
    int usedTokens,
    int remainingTokens
) {
}
