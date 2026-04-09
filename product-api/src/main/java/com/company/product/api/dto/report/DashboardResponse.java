package com.company.product.api.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    long totalPayouts,
    long createdPayouts,
    long preparedPayouts,
    long paidPayouts,
    long cancelledPayouts,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal pendingAmount,
    List<MonthlyPoint> monthlyTotals,
    List<StatusPoint> statusDistribution
) {

    public record MonthlyPoint(String label, BigDecimal amount) {
    }

    public record StatusPoint(String status, long count) {
    }
}
