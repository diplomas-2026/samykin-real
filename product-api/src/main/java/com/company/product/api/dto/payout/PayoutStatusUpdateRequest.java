package com.company.product.api.dto.payout;

import com.company.product.api.entity.PayoutStatus;
import jakarta.validation.constraints.NotNull;

public record PayoutStatusUpdateRequest(
    @NotNull(message = "Статус обязателен")
    PayoutStatus status
) {
}
