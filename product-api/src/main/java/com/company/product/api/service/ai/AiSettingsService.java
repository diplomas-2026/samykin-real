package com.company.product.api.service.ai;

import com.company.product.api.config.AppProperties;
import com.company.product.api.dto.settings.AiSettingsResponse;
import com.company.product.api.dto.settings.AiStyleUpdateRequest;
import com.company.product.api.entity.AiSettings;
import com.company.product.api.repository.AiSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final AiSettingsRepository aiSettingsRepository;
    private final AppProperties appProperties;

    @Transactional
    public AiSettingsResponse getSettings() {
        AiSettings settings = normalizeSettings(getEntity());
        return new AiSettingsResponse(settings.getStyleName(), settings.getStyleInstruction(), settings.getUpdatedAt());
    }

    @Transactional
    public AiSettingsResponse update(AiStyleUpdateRequest request) {
        AiSettings settings = getEntity();
        settings.setStyleName(normalizeText(request.styleName().trim()));
        settings.setStyleInstruction(normalizeText(request.styleInstruction().trim()));
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
        settings.setStyleName(normalizeText(appProperties.ai().defaultStyleName()));
        settings.setStyleInstruction(normalizeText(appProperties.ai().defaultStyleInstruction()));
        return aiSettingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public AiSettings getEntity() {
        return aiSettingsRepository.findById(1L).orElseGet(this::ensureSettings);
    }

    private AiSettings normalizeSettings(AiSettings settings) {
        String normalizedStyleName = normalizeText(settings.getStyleName());
        String normalizedInstruction = normalizeText(settings.getStyleInstruction());
        if (!Objects.equals(normalizedStyleName, settings.getStyleName())
            || !Objects.equals(normalizedInstruction, settings.getStyleInstruction())) {
            settings.setStyleName(normalizedStyleName);
            settings.setStyleInstruction(normalizedInstruction);
            return aiSettingsRepository.save(settings);
        }
        return settings;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (!looksLikeMojibake(value)) {
            return value;
        }
        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private boolean looksLikeMojibake(String value) {
        return value.contains("Ð") || value.contains("Ñ") || value.contains("Â");
    }
}
