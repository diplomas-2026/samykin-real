package com.company.product.api.service.ai;

import com.company.product.api.config.AppProperties;
import com.company.product.api.dto.settings.AiSettingsResponse;
import com.company.product.api.dto.settings.AiStyleUpdateRequest;
import com.company.product.api.entity.AiSettings;
import com.company.product.api.repository.AiSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public AiSettingsResponse getSettings() {
        AiSettings settings = getEntity();
        return new AiSettingsResponse(settings.getStyleName(), settings.getStyleInstruction(), settings.getUpdatedAt());
    }

    @Transactional
    public AiSettingsResponse update(AiStyleUpdateRequest request) {
        AiSettings settings = getEntity();
        settings.setStyleName(request.styleName().trim());
        settings.setStyleInstruction(request.styleInstruction().trim());
        AiSettings saved = aiSettingsRepository.save(settings);
        return new AiSettingsResponse(saved.getStyleName(), saved.getStyleInstruction(), saved.getUpdatedAt());
    }

    @Transactional
    public AiSettings ensureSettings() {
        if (aiSettingsRepository.existsById(1L)) {
            return aiSettingsRepository.findById(1L).orElseThrow();
        }
        AiSettings settings = new AiSettings();
        settings.setId(1L);
        settings.setStyleName(appProperties.ai().defaultStyleName());
        settings.setStyleInstruction(appProperties.ai().defaultStyleInstruction());
        return aiSettingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public AiSettings getEntity() {
        return aiSettingsRepository.findById(1L).orElseGet(this::ensureSettings);
    }
}
