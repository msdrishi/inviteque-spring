package com.invitique.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String userId;
    private String email;
    private String name;
    private Set<String> roles;
}

