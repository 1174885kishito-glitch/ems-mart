package com.springmart.service;

import com.springmart.dto.LoginRequest;
import com.springmart.dto.LoginResponse;
import com.springmart.entity.User;
import com.springmart.repository.UserRepository;
import com.springmart.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        System.out.println("Login attempt for user: " + request.getUserName());
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> {
                    System.out.println("User not found: " + request.getUserName());
                    return new RuntimeException("ユーザー名またはパスワードが正しくありません");
                });

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password match result: " + matches);

        if (!matches) {
            throw new RuntimeException("ユーザー名またはパスワードが正しくありません");
        }

        System.out.println("Generating token for user: " + user.getUserName());
        String token = jwtTokenProvider.generateToken(user.getUserName(), user.getRole());
        return new LoginResponse(token);
    }
}
