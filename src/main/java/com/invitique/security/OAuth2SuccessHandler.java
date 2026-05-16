package com.invitique.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String googleId = oauthUser.getAttribute("sub");

        System.out.println("OAuth2 Login Attempt for email: " + email);
        System.out.println("OAuth2 User Attributes: " + oauthUser.getAttributes());

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .provider(User.AuthProvider.GOOGLE)
                    .build();
            return userRepository.save(newUser);
        });

        // Update google ID if missing
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        // Redirect back to frontend with token
        String targetUrl = "http://localhost:5173/login-success?token=" + token +
                           "&userId=" + user.getId().toString() +
                           "&name=" + user.getName().replace(" ", "%20") +
                           "&email=" + user.getEmail();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
