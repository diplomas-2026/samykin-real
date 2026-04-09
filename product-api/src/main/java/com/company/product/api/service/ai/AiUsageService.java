package com.company.product.api.service.ai;

import com.company.product.api.config.AppProperties;
import com.company.product.api.dto.report.AiUsageResponse;
import com.company.product.api.entity.AiUsageDaily;
import com.company.product.api.repository.AiUsageDailyRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiUsageService {

    private final AiUsageDailyRepository aiUsageDailyRepository;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public AiUsageResponse getUsage() {
        LocalDate today = LocalDate.now(zoneId());
        int used = aiUsageDailyRepository.findById(today).map(AiUsageDaily::getUsedTokens).orElse(0);
        int limit = appProperties.ai().dailyTokenLimit();
        return new AiUsageResponse(limit, used, Math.max(limit - used, 0), nextReset());
    }

    @Transactional
    public AiUsageResponse consume(int tokens) {
        if (tokens <= 0) {
            return getUsage();
        }
        LocalDate today = LocalDate.now(zoneId());
        AiUsageDaily usage = aiUsageDailyRepository.findById(today).orElseGet(() -> {
            AiUsageDaily created = new AiUsageDaily();
            created.setUsageDate(today);
            created.setUsedTokens(0);
            return created;
        });
        int limit = appProperties.ai().dailyTokenLimit();
        if (usage.getUsedTokens() + tokens > limit) {
            throw new IllegalArgumentException("Дневной лимит AI исчерпан");
        }
        usage.setUsedTokens(usage.getUsedTokens() + tokens);
        aiUsageDailyRepository.save(usage);
        return new AiUsageResponse(limit, usage.getUsedTokens(), limit - usage.getUsedTokens(), nextReset());
    }

    public OffsetDateTime nextReset() {
        return LocalDate.now(zoneId()).plusDays(1).atStartOfDay(zoneId()).toOffsetDateTime();
    }

    private ZoneId zoneId() {
        return ZoneId.of(appProperties.ai().zoneId());
    }
}
