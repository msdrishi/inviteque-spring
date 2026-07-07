package com.invitique.web.controller;

import com.invitique.dto.request.LoginRequest;
import com.invitique.dto.request.RegisterRequest;
import com.invitique.dto.response.AuthResponse;
import com.invitique.service.api.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        authService.sendOtp(phoneNumber);
        return ResponseEntity.ok(Map.of("success", true, "message", "OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String code = request.get("code");
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number and code are required");
        }
        return ResponseEntity.ok(authService.verifyOtp(phoneNumber, code));
    }
}
