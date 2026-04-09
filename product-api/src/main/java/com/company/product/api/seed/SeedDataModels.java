package com.company.product.api.seed;

import com.company.product.api.entity.PayoutStatus;
import com.company.product.api.entity.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SeedDataModels(
    List<SeedUser> users,
    List<SeedPayout> payouts
) {

    public record SeedUser(
        String email,
        String password,
        String fullName,
        String department,
        String position,
        String employeeCode,
        UserRole role,
        boolean active
    ) {
    }

    public record SeedPayout(
        String payoutCode,
        String employeeEmail,
        String createdByEmail,
        String payoutType,
        BigDecimal amount,
        LocalDate payoutDate,
        PayoutStatus status,
        String basis,
        String comment,
        String payoutNote
    ) {
    }
}
