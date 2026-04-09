package com.company.product.api.service;

import com.company.product.api.dto.payout.PayoutResponse;
import com.company.product.api.entity.Payout;
import org.springframework.stereotype.Component;

@Component
public class PayoutMapper {

    public PayoutResponse toResponse(Payout payout) {
        return new PayoutResponse(
            payout.getId(),
            payout.getPayoutCode(),
            payout.getEmployee().getId(),
            payout.getEmployee().getFullName(),
            payout.getEmployee().getEmail(),
            payout.getPayoutType(),
            payout.getAmount(),
            payout.getPayoutDate(),
            payout.getStatus(),
            payout.getBasis(),
            payout.getComment(),
            payout.getPayoutNote(),
            payout.getPreparedAt(),
            payout.getPaidAt(),
            payout.getCreatedAt(),
            payout.getCreatedBy().getFullName()
        );
    }
}
