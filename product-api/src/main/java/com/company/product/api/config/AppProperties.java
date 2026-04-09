package com.company.product.api.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Jwt jwt,
    String seedDataDir,
    String usersFile,
    Ai ai
) {

    public record Jwt(
        @NotBlank String secret,
        @Min(1) long expirationMinutes
    ) {
    }

    public record Ai(
        @Min(1) int dailyTokenLimit,
        @NotBlank String zoneId,
        @NotBlank String defaultStyleName,
        @NotBlank String defaultStyleInstruction
    ) {
    }
}
