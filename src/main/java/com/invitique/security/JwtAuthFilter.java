package com.invitique.security;

import com.invitique.domain.model.User;
import com.invitique.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            System.out.println("[JWT DEBUG] Received Authorization Header: " + authHeader);
            try {
                boolean isValid = jwtTokenProvider.validateToken(token);
                System.out.println("[JWT DEBUG] Token validation result: " + isValid);
                if (isValid) {
                    UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                    System.out.println("[JWT DEBUG] Extracted User ID: " + userId);
                    User user = userRepository.findById(userId).orElse(null);
                    System.out.println("[JWT DEBUG] User found in database: " + (user != null));
                    if (user != null) {
                        System.out.println("[JWT DEBUG] Authenticated user: " + user.getEmail());
                        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(java.util.stream.Collectors.toList());
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                authorities
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                System.out.println("[JWT DEBUG] Exception during token parsing: " + e.getMessage());
            }
        } else if (authHeader != null) {
            System.out.println("[JWT DEBUG] Received Authorization Header but could not extract token: " + authHeader);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
