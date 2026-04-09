package com.company.product.api.dto.report;

import java.time.OffsetDateTime;

public record AiUsageResponse(
    int dailyLimit,
    int usedTokens,
    int remainingTokens,
    OffsetDateTime resetsAt
) {
}
