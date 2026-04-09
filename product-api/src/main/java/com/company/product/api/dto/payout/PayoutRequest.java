package com.company.product.api.dto.payout;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PayoutRequest(
    @NotNull(message = "Сотрудник обязателен")
    Long employeeId,
    @NotBlank(message = "Тип выплаты обязателен")
    String payoutType,
    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше нуля")
    BigDecimal amount,
    @NotNull(message = "Дата выплаты обязательна")
    LocalDate payoutDate,
    @NotBlank(message = "Основание обязательно")
    String basis,
    @NotBlank(message = "Комментарий обязателен")
    String comment,
    String payoutNote
) {
}
