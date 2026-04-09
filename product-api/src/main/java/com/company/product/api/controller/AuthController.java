package com.company.product.api.controller;

import com.company.product.api.dto.auth.LoginRequest;
import com.company.product.api.dto.auth.LoginResponse;
import com.company.product.api.dto.auth.MeResponse;
import com.company.product.api.service.AuthService;
import com.company.product.api.service.UserMapper;
import com.company.product.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        var user = userService.findByEmail(authentication.getName());
        return new MeResponse(userMapper.toUserResponse(user));
    }
}
