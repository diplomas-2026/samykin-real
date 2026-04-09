package com.company.product.api.dto.payout;

public record AiCommentResponse(
    String comment,
    int usedTokens,
    int remainingTokens
) {
}
