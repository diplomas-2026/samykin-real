package com.company.product.api.dto.payout;

import com.company.product.api.entity.PayoutStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record PayoutResponse(
    Long id,
    String payoutCode,
    Long employeeId,
    String employeeName,
    String employeeEmail,
    String payoutType,
    BigDecimal amount,
    LocalDate payoutDate,
    PayoutStatus status,
    String basis,
    String comment,
    String payoutNote,
    OffsetDateTime preparedAt,
    OffsetDateTime paidAt,
    OffsetDateTime createdAt,
    String createdByName
) {
}
