package com.company.product.api.dto.auth;

import com.company.product.api.dto.user.UserResponse;

public record LoginResponse(
    String token,
    UserResponse user
) {
}
