package com.company.product.api.dto.ai;

import com.company.product.api.entity.PayoutStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AiAssistantPayoutResponse(
    Long id,
    String payoutCode,
    String payoutType,
    BigDecimal amount,
    LocalDate payoutDate,
    PayoutStatus status
) {
}
