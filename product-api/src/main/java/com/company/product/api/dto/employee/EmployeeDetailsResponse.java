package com.company.product.api.dto.employee;

import com.company.product.api.dto.payout.PayoutResponse;
import java.util.List;

public record EmployeeDetailsResponse(
    EmployeeResponse employee,
    List<PayoutResponse> payouts
) {
}
