package com.company.product.api.service;

import com.company.product.api.dto.employee.EmployeeResponse;
import com.company.product.api.dto.user.UserResponse;
import com.company.product.api.entity.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(UserAccount user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getDepartment(),
            user.getPosition(),
            user.getEmployeeCode(),
            user.getPhotoUrl(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt()
        );
    }

    public EmployeeResponse toEmployeeResponse(UserAccount user) {
        return new EmployeeResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getDepartment(),
            user.getPosition(),
            user.getEmployeeCode(),
            user.getPhotoUrl()
        );
    }
}
