package com.company.product.api.service;

import com.company.product.api.dto.employee.EmployeeResponse;
import com.company.product.api.dto.user.UserPhotoUpdateRequest;
import com.company.product.api.dto.user.UserRequest;
import com.company.product.api.dto.user.UserResponse;
import com.company.product.api.entity.UserAccount;
import com.company.product.api.entity.UserRole;
import com.company.product.api.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userAccountRepository.findAll().stream()
            .sorted((left, right) -> left.getFullName().compareToIgnoreCase(right.getFullName()))
            .map(userMapper::toUserResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(getEntity(id));
    }

    @Transactional
    public UserResponse createUser(UserRequest request, String actorEmail) {
        userAccountRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        });
        userAccountRepository.findByEmployeeCode(request.employeeCode()).ifPresent(user -> {
            throw new IllegalArgumentException("Пользователь с таким табельным номером уже существует");
        });

        UserAccount user = new UserAccount();
        apply(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        UserAccount user = getEntity(id);
        userAccountRepository.findByEmailIgnoreCase(request.email())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            });
        userAccountRepository.findByEmployeeCode(request.employeeCode())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Пользователь с таким табельным номером уже существует");
            });
        apply(user, request);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getEmployees() {
        return userAccountRepository.findAllByRoleOrderByFullNameAsc(UserRole.EMPLOYEE)
            .stream()
            .map(userMapper::toEmployeeResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return userAccountRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public UserResponse updateOwnPhoto(String email, UserPhotoUpdateRequest request) {
        UserAccount user = findByEmail(email);
        if (user.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalArgumentException("Фотографию профиля может изменить только сотрудник");
        }
        String photoUrl = request.photoUrl().trim();
        if (!photoUrl.startsWith("data:image/")) {
            throw new IllegalArgumentException("Поддерживаются только изображения");
        }
        user.setPhotoUrl(photoUrl);
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserAccount getEntity(Long id) {
        return userAccountRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private void apply(UserAccount user, UserRequest request) {
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setDepartment(request.department().trim());
        user.setPosition(request.position().trim());
        user.setEmployeeCode(request.employeeCode().trim());
        user.setRole(request.role());
        user.setActive(request.active());
    }
}
