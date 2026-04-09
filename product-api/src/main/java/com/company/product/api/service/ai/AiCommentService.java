package com.company.product.api.service.ai;

import com.company.product.api.dto.payout.AiCommentRequest;
import com.company.product.api.dto.payout.AiCommentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiCommentService {

    private final AiSettingsService aiSettingsService;
    private final AiUsageService aiUsageService;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    @org.springframework.beans.factory.annotation.Value("${spring.ai.gigachat.auth.bearer.api-key:}")
    private String gigaChatApiKey;
    @org.springframework.beans.factory.annotation.Value("${spring.ai.gigachat.auth.scope:GIGACHAT_API_PERS}")
    private String gigaChatScope;
    @org.springframework.beans.factory.annotation.Value("${spring.ai.gigachat.base-url:https://gigachat.devices.sberbank.ru/api/v1}")
    private String gigaChatBaseUrl;
    @org.springframework.beans.factory.annotation.Value("${spring.ai.gigachat.auth.bearer.url:https://ngw.devices.sberbank.ru:9443/api/v2/oauth}")
    private String gigaChatAuthUrl;

    public AiCommentResponse generateComment(AiCommentRequest request) {
        var settings = aiSettingsService.getEntity();
        String systemPrompt = """
            Ты помощник бухгалтерской системы ИП Самыкин.
            Генерируй только один готовый служебный комментарий на русском языке.
            Не используй списки, кавычки и markdown.
            Комментарий должен подходить для карточки денежной выплаты.
            Соблюдай стиль: %s
            """.formatted(settings.getStyleInstruction());

        String userPrompt = """
            Сотрудник: %s
            Тип выплаты: %s
            Сумма: %s
            Основание: %s
            Текущий комментарий: %s
            """.formatted(
            request.employeeName(),
            request.payoutType(),
            request.amount(),
            request.basis(),
            StringUtils.hasText(request.existingComment()) ? request.existingComment() : "нет"
        );

        String accessToken = requestAccessToken();
        String content = requestCompletion(accessToken, systemPrompt, userPrompt);

        int usedTokens = estimateTokens(systemPrompt + userPrompt + content);
        var usage = aiUsageService.consume(usedTokens);
        return new AiCommentResponse(content == null ? "" : content.trim(), usedTokens, usage.remainingTokens());
    }

    private String requestAccessToken() {
        if (!StringUtils.hasText(gigaChatApiKey)) {
            throw new IllegalStateException("Не настроен GigaChat token");
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("scope", gigaChatScope);

        JsonNode response = restClientBuilder.build()
            .post()
            .uri(gigaChatAuthUrl)
            .header("Authorization", "Basic " + gigaChatApiKey)
            .header("RqUID", UUID.randomUUID().toString())
            .header("Accept", "application/json")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(JsonNode.class);

        if (response == null || response.path("access_token").asText().isBlank()) {
            throw new IllegalStateException("Не удалось получить access token GigaChat");
        }
        return response.path("access_token").asText();
    }

    private String requestCompletion(String accessToken, String systemPrompt, String userPrompt) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", "GigaChat");
            payload.set("messages", objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode()
                    .put("role", "system")
                    .put("content", systemPrompt))
                .add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", userPrompt)));

            JsonNode response = restClientBuilder.build()
                .post()
                .uri(gigaChatBaseUrl + "/chat/completions")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(JsonNode.class);

            String content = response == null ? "" : response.path("choices").path(0).path("message").path("content").asText();
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("GigaChat вернул пустой ответ");
            }
            return content;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить ответ от GigaChat: " + exception.getMessage(), exception);
        }
    }

    private int estimateTokens(String text) {
        return Math.max(100, text.length() / 3);
    }
}
