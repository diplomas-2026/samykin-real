package com.company.product.api.dto.employee;

public record EmployeeResponse(
    Long id,
    String fullName,
    String email,
    String department,
    String position,
    String employeeCode,
    String photoUrl
) {
}
