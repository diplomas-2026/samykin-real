package com.company.product.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.product.api.config.AppProperties;
import com.company.product.api.repository.AiUsageDailyRepository;
import com.company.product.api.service.ai.AiUsageService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiUsageServiceTest {

    @Mock
    private AiUsageDailyRepository aiUsageDailyRepository;

    private AiUsageService aiUsageService;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties(
            new AppProperties.Jwt("12345678901234567890123456789012", 120),
            "seed-data",
            "users.txt",
            new AppProperties.Ai(20000, "Europe/Samara", "Деловой", "Официальный стиль")
        );
        aiUsageService = new AiUsageService(aiUsageDailyRepository, properties);
    }

    @Test
    void shouldReturnFullLimitWhenNoUsageExists() {
        assertThat(aiUsageService.getUsage().remainingTokens()).isEqualTo(20000);
    }

    @Test
    void shouldRejectOverLimitUsage() {
        assertThatThrownBy(() -> aiUsageService.consume(21000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("лимит");
    }
}
