package com.company.product.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPhotoUpdateRequest(
    @NotBlank(message = "Фотография обязательна")
    @Size(max = 2_500_000, message = "Фотография слишком большая")
    String photoUrl
) {
}
