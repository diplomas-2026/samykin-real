package com.company.product.api.service.ai;

import com.company.product.api.dto.ai.AiAssistantMessageRequest;
import com.company.product.api.dto.ai.AiAssistantMessageResponse;
import com.company.product.api.dto.ai.AiChatTurnRequest;
import com.company.product.api.repository.PayoutRepository;
import com.company.product.api.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final PayoutRepository payoutRepository;
    private final UserService userService;
    private final AiUsageService aiUsageService;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.gigachat.auth.bearer.api-key:}")
    private String gigaChatApiKey;
    @Value("${spring.ai.gigachat.auth.scope:GIGACHAT_API_PERS}")
    private String gigaChatScope;
    @Value("${spring.ai.gigachat.base-url:https://gigachat.devices.sberbank.ru/api/v1}")
    private String gigaChatBaseUrl;
    @Value("${spring.ai.gigachat.auth.bearer.url:https://ngw.devices.sberbank.ru:9443/api/v2/oauth}")
    private String gigaChatAuthUrl;

    @Transactional(readOnly = true)
    public AiAssistantMessageResponse chat(String employeeEmail, AiAssistantMessageRequest request) {
        var employee = userService.findByEmail(employeeEmail);
        var payouts = payoutRepository.findAllDetailedByEmployeeEmail(employeeEmail);

        String payoutsContext = payouts.isEmpty()
            ? "У сотрудника пока нет зарегистрированных выплат."
            : payouts.stream()
                .map(payout -> """
                    Код: %s; Тип: %s; Сумма: %s; Дата: %s; Статус: %s; Комментарий: %s
                    """.formatted(
                    payout.getPayoutCode(),
                    payout.getPayoutType(),
                    payout.getAmount(),
                    payout.getPayoutDate(),
                    payout.getStatus(),
                    StringUtils.hasText(payout.getComment()) ? payout.getComment() : "нет"
                ))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("У сотрудника пока нет зарегистрированных выплат.");

        String systemPrompt = """
            Ты AI-помощник сотрудника в системе денежных выплат ИП Самыкин.
            Отвечай только по выплатам текущего сотрудника и только на русском языке.
            Не выдумывай данные, которых нет в контексте.
            Если вопрос не относится к выплатам сотрудника, вежливо сообщи, что можешь помогать только по его выплатам.
            Объясняй статусы, даты, суммы и комментарии понятным деловым языком.
            """;

        String contextPrompt = """
            Текущий сотрудник: %s
            Email сотрудника: %s
            Выплаты сотрудника:
            %s
            """.formatted(employee.getFullName(), employee.getEmail(), payoutsContext);

        String accessToken = requestAccessToken();
        String reply = requestCompletion(accessToken, systemPrompt, contextPrompt, request.history(), request.message());
        int usedTokens = estimateTokens(systemPrompt + contextPrompt + request.message() + reply);
        var usage = aiUsageService.consume(usedTokens);
        return new AiAssistantMessageResponse(reply.trim(), usedTokens, usage.remainingTokens());
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

    private String requestCompletion(String accessToken, String systemPrompt, String contextPrompt, List<AiChatTurnRequest> history, String message) {
        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", "GigaChat");

            ArrayNode messages = objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode()
                    .put("role", "system")
                    .put("content", systemPrompt))
                .add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", contextPrompt));

            if (history != null) {
                history.stream()
                    .limit(10)
                    .forEach(turn -> messages.add(objectMapper.createObjectNode()
                        .put("role", turn.role())
                        .put("content", turn.content())));
            }

            messages.add(objectMapper.createObjectNode()
                .put("role", "user")
                .put("content", message));

            payload.set("messages", messages);

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
