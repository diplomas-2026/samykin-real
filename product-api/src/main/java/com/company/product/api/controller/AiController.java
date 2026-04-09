package com.company.product.api.controller;

import com.company.product.api.dto.ai.AiAssistantMessageRequest;
import com.company.product.api.dto.ai.AiAssistantMessageResponse;
import com.company.product.api.dto.report.AiUsageResponse;
import com.company.product.api.dto.settings.AiSettingsResponse;
import com.company.product.api.dto.settings.AiStyleUpdateRequest;
import com.company.product.api.service.ai.AiAssistantService;
import com.company.product.api.service.ai.AiSettingsService;
import com.company.product.api.service.ai.AiUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService aiAssistantService;
    private final AiSettingsService aiSettingsService;
    private final AiUsageService aiUsageService;

    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public AiSettingsResponse getSettings() {
        return aiSettingsService.getSettings();
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public AiSettingsResponse updateSettings(@Valid @RequestBody AiStyleUpdateRequest request) {
        return aiSettingsService.update(request);
    }

    @GetMapping("/usage")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'EMPLOYEE')")
    public AiUsageResponse getUsage() {
        return aiUsageService.getUsage();
    }

    @PostMapping("/assistant/chat")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public AiAssistantMessageResponse chat(@Valid @RequestBody AiAssistantMessageRequest request, Authentication authentication) {
        return aiAssistantService.chat(authentication.getName(), request);
    }
}
