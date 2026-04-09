package com.company.product.api.service;

import com.company.product.api.dto.auth.LoginRequest;
import com.company.product.api.dto.auth.LoginResponse;
import com.company.product.api.dto.user.UserResponse;
import com.company.product.api.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final UserMapper userMapper;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userService.findByEmail(request.email());
        String token = jwtTokenService.generate(user);
        UserResponse response = userMapper.toUserResponse(user);
        return new LoginResponse(token, response);
    }
}
