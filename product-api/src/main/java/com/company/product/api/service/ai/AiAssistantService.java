package com.company.product.api.service.ai;

import com.company.product.api.dto.ai.AiAssistantMessageRequest;
import com.company.product.api.dto.ai.AiAssistantMessageResponse;
import com.company.product.api.dto.ai.AiAssistantPayoutResponse;
import com.company.product.api.repository.PayoutRepository;
import com.company.product.api.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
            Верни строго JSON-объект такого вида:
            {
              "message": "ответ в markdown",
              "payoutCodes": ["PAY-00001", "PAY-00002"]
            }
            В message можно использовать markdown.
            Но верни JSON корректно: если нужен перенос строки внутри message, используй \\n, а не сырой перевод строки.
            В payoutCodes укажи только те коды выплат, которые реально относятся к ответу.
            Если релевантных выплат нет, верни пустой массив.
            """;

        String contextPrompt = """
            Текущий сотрудник: %s
            Email сотрудника: %s
            Выплаты сотрудника:
            %s
            """.formatted(employee.getFullName(), employee.getEmail(), payoutsContext);

        String accessToken = requestAccessToken();
        JsonNode reply = requestCompletion(accessToken, systemPrompt, contextPrompt, request.message());
        String message = reply.path("message").asText();
        List<String> payoutCodes = extractPayoutCodes(reply.path("payoutCodes"));
        List<AiAssistantPayoutResponse> relatedPayouts = payouts.stream()
            .filter(payout -> payoutCodes.contains(payout.getPayoutCode()))
            .map(payout -> new AiAssistantPayoutResponse(
                payout.getId(),
                payout.getPayoutCode(),
                payout.getPayoutType(),
                payout.getAmount(),
                payout.getPayoutDate(),
                payout.getStatus()
            ))
            .toList();

        int usedTokens = estimateTokens(systemPrompt + contextPrompt + request.message() + message);
        var usage = aiUsageService.consume(usedTokens);
        return new AiAssistantMessageResponse(message.trim(), relatedPayouts, usedTokens, usage.remainingTokens());
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

    private JsonNode requestCompletion(String accessToken, String systemPrompt, String contextPrompt, String message) {
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
            JsonNode parsed = parseAssistantResponse(content);
            if (!StringUtils.hasText(parsed.path("message").asText())) {
                throw new IllegalStateException("GigaChat вернул пустой message");
            }
            return parsed;
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось получить ответ от GigaChat: " + exception.getMessage(), exception);
        }
    }

    private JsonNode parseAssistantResponse(String content) throws Exception {
        String normalized = escapeRawControlCharsInsideStrings(normalizeJsonContent(content));
        try {
            return objectMapper.readTree(normalized);
        } catch (Exception ignored) {
            ObjectNode fallback = JsonNodeFactory.instance.objectNode();
            fallback.put("message", content.trim());
            fallback.set("payoutCodes", JsonNodeFactory.instance.arrayNode());
            return fallback;
        }
    }

    private String normalizeJsonContent(String content) {
        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "");
        }
        return normalized.trim();
    }

    private String escapeRawControlCharsInsideStrings(String input) {
        StringBuilder result = new StringBuilder(input.length() + 32);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (escaped) {
                result.append(current);
                escaped = false;
                continue;
            }

            if (current == '\\') {
                result.append(current);
                escaped = true;
                continue;
            }

            if (current == '"') {
                result.append(current);
                inString = !inString;
                continue;
            }

            if (inString) {
                if (current == '\n') {
                    result.append("\\n");
                    continue;
                }
                if (current == '\r') {
                    result.append("\\r");
                    continue;
                }
                if (current == '\t') {
                    result.append("\\t");
                    continue;
                }
            }

            result.append(current);
        }

        return result.toString();
    }

    private List<String> extractPayoutCodes(JsonNode payoutCodesNode) {
        if (payoutCodesNode == null || !payoutCodesNode.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(payoutCodesNode.spliterator(), false)
            .map(JsonNode::asText)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    private int estimateTokens(String text) {
        return Math.max(100, text.length() / 3);
    }
}
