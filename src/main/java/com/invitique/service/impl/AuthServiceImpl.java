package com.invitique.service.impl;

import com.invitique.domain.model.OtpVerification;
import com.invitique.domain.model.Role;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.OtpVerificationRepository;
import com.invitique.domain.repository.UserRepository;
import com.invitique.dto.request.LoginRequest;
import com.invitique.dto.request.RegisterRequest;
import com.invitique.dto.response.AuthResponse;
import com.invitique.security.JwtTokenProvider;
import com.invitique.service.api.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OtpVerificationRepository otpVerificationRepository;
    private final WhatsAppService whatsAppService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(User.AuthProvider.LOCAL)
                .build();

        user.addRole(Role.USER);
        User savedUser = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId().toString())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .roles(savedUser.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .build();
    }

    @Override
    @Transactional
    public void sendOtp(String phoneNumber) {
        // Generate a 6-digit random code
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // Deactivate previous unverified OTPs for this phone number
        LocalDateTime now = LocalDateTime.now();
        
        OtpVerification otpVerification = OtpVerification.builder()
                .phoneNumber(phoneNumber)
                .code(code)
                .expiryTime(now.plusMinutes(5))
                .verified(false)
                .build();
                
        otpVerificationRepository.save(otpVerification);
        
        // Send OTP via WhatsApp
        whatsAppService.sendOtp(phoneNumber, code);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(String phoneNumber, String code) {
        LocalDateTime now = LocalDateTime.now();
        
        OtpVerification otpVerification = otpVerificationRepository
                .findFirstByPhoneNumberAndVerifiedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(phoneNumber, now)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (!otpVerification.getCode().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        // Find or create customer user
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseGet(() -> {
            User newUser = User.builder()
                    .name(phoneNumber)
                    .phoneNumber(phoneNumber)
                    .provider(User.AuthProvider.LOCAL)
                    .build();
            newUser.addRole(Role.USER);
            return userRepository.save(newUser);
        });

        // Generate token (using phoneNumber as subject/email since email is null)
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhoneNumber());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getPhoneNumber())
                .name(user.getName())
                .roles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}
