package com.invitique.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    @Value("${whatsapp.api-url:https://graph.facebook.com/v19.0}")
    private String apiUrl;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.template-name:}")
    private String templateName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String phoneNumber, String otp) {
        if (accessToken == null || accessToken.trim().isEmpty() || "YOUR_ACCESS_TOKEN".equals(accessToken)) {
            log.info("========================================");
            log.info("=== WHATSAPP MOCK SENDER ===");
            log.info("To: {}", phoneNumber);
            log.info("Message: Your Inviteque verification code is: {}", otp);
            log.info("========================================");
            return;
        }

        try {
            String url = String.format("%s/%s/messages", apiUrl, phoneNumberId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("to", formatPhoneNumber(phoneNumber));

            if (templateName != null && !templateName.trim().isEmpty()) {
                body.put("type", "template");
                Map<String, Object> template = new HashMap<>();
                template.put("name", templateName);
                
                Map<String, String> language = new HashMap<>();
                language.put("code", "en_US");
                template.put("language", language);

                Map<String, Object> bodyParam = new HashMap<>();
                bodyParam.put("type", "text");
                bodyParam.put("text", otp);

                Map<String, Object> component = new HashMap<>();
                component.put("type", "body");
                component.put("parameters", List.of(bodyParam));

                template.put("components", List.of(component));
                body.put("template", template);
            } else {
                body.put("type", "text");
                Map<String, Object> text = new HashMap<>();
                text.put("body", String.format("Your Inviteque OTP is: %s. Valid for 5 minutes.", otp));
                body.put("text", text);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("WhatsApp OTP sent. Response: {}", response.getBody());

        } catch (Exception e) {
            log.error("Failed to send WhatsApp: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send OTP via WhatsApp: " + e.getMessage());
        }
    }

    private String formatPhoneNumber(String rawPhone) {
        String clean = rawPhone.replaceAll("[^0-9]", "");
        if (clean.length() == 10) {
            return "91" + clean;
        }
        return clean;
    }
}
