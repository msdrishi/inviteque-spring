package com.invitique.service.api;

import com.invitique.dto.request.LoginRequest;
import com.invitique.dto.request.RegisterRequest;
import com.invitique.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void sendOtp(String phoneNumber);
    AuthResponse verifyOtp(String phoneNumber, String code);
}
