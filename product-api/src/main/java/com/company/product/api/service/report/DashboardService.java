package com.company.product.api.service.report;

import com.company.product.api.dto.report.DashboardResponse;
import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.repository.PayoutRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PayoutRepository payoutRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<com.company.product.api.entity.Payout> payouts = payoutRepository.findAllDetailed();
        BigDecimal totalAmount = payouts.stream().map(p -> p.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paidAmount = payouts.stream()
            .filter(p -> p.getStatus() == PayoutStatus.PAID)
            .map(p -> p.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingAmount = totalAmount.subtract(paidAmount);

        var monthlyTotals = payouts.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                p -> YearMonth.from(p.getPayoutDate()),
                java.util.TreeMap::new,
                java.util.stream.Collectors.mapping(
                    com.company.product.api.entity.Payout::getAmount,
                    java.util.stream.Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ))
            .entrySet()
            .stream()
            .map(entry -> new DashboardResponse.MonthlyPoint(
                entry.getKey().getMonth().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("ru")) + " " + entry.getKey().getYear(),
                entry.getValue()
            ))
            .toList();

        var statusDistribution = List.of(
            new DashboardResponse.StatusPoint("CREATED", payouts.stream().filter(p -> p.getStatus() == PayoutStatus.CREATED).count()),
            new DashboardResponse.StatusPoint("PREPARED", payouts.stream().filter(p -> p.getStatus() == PayoutStatus.PREPARED).count()),
            new DashboardResponse.StatusPoint("PAID", payouts.stream().filter(p -> p.getStatus() == PayoutStatus.PAID).count()),
            new DashboardResponse.StatusPoint("CANCELLED", payouts.stream().filter(p -> p.getStatus() == PayoutStatus.CANCELLED).count())
        );

        return new DashboardResponse(
            payouts.size(),
            payouts.stream().filter(p -> p.getStatus() == PayoutStatus.CREATED).count(),
            payouts.stream().filter(p -> p.getStatus() == PayoutStatus.PREPARED).count(),
            payouts.stream().filter(p -> p.getStatus() == PayoutStatus.PAID).count(),
            payouts.stream().filter(p -> p.getStatus() == PayoutStatus.CANCELLED).count(),
            totalAmount,
            paidAmount,
            pendingAmount,
            monthlyTotals,
            statusDistribution
        );
    }
}
