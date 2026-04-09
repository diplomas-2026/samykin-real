package com.company.product.api.dto.user;

import com.company.product.api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @Email(message = "Введите корректный email")
    @NotBlank(message = "Email обязателен")
    String email,
    @NotBlank(message = "ФИО обязательно")
    String fullName,
    @NotBlank(message = "Подразделение обязательно")
    String department,
    @NotBlank(message = "Должность обязательна")
    String position,
    @NotBlank(message = "Табельный номер обязателен")
    String employeeCode,
    @NotNull(message = "Роль обязательна")
    UserRole role,
    boolean active,
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    String password
) {
}
