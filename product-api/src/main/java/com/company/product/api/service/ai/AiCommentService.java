package com.company.product.api.service.ai;

import com.company.product.api.dto.payout.AiCommentRequest;
import com.company.product.api.dto.payout.AiCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.Builder;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiCommentService {

    private final ChatModel chatModel;
    private final AiSettingsService aiSettingsService;
    private final AiUsageService aiUsageService;

    public AiCommentResponse generateComment(AiCommentRequest request) {
        Builder builder = ChatClient.builder(chatModel);
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

        String content = builder.build()
            .prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();

        int usedTokens = estimateTokens(systemPrompt + userPrompt + content);
        var usage = aiUsageService.consume(usedTokens);
        return new AiCommentResponse(content == null ? "" : content.trim(), usedTokens, usage.remainingTokens());
    }

    private int estimateTokens(String text) {
        return Math.max(100, text.length() / 3);
    }
}
