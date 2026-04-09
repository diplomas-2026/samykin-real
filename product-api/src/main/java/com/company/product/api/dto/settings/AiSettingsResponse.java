package com.company.product.api.dto.settings;

import java.time.OffsetDateTime;

public record AiSettingsResponse(
    String styleName,
    String styleInstruction,
    OffsetDateTime updatedAt
) {
}
