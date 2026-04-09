package com.company.product.api.dto.user;

import com.company.product.api.entity.UserRole;
import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String department,
    String position,
    String employeeCode,
    String photoUrl,
    UserRole role,
    boolean active,
    OffsetDateTime createdAt
) {
}
