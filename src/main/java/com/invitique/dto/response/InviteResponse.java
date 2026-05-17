package com.invitique.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteResponse {
    private UUID id;
    private UUID inviteId; // Expectation endpoint: inviteId
    private Boolean success; // Expectation endpoint: success status
    private String templateId;
    private String code;
    private String status;
    private Map<String, Object> coupleData;
    private Map<String, Object> heroData;
    private Map<String, Object> storyData;
    private Map<String, Object> invitationData;
    private Map<String, Object> eventData;
    private Map<String, Object> venueData;
    private Map<String, Object> scheduleData;
    private Map<String, Object> rsvpData;
    private java.time.LocalDateTime createdAt;

    // Flat fields populated dynamically from JSONB data for Modern Builder integration
    private String coupleNames;
    private String groomName;
    private String brideName;
    private String mahalName;
    private Map<String, String> weddingDate;
    private String venueCity;
    private String venueName;
    private List<String> photos;
    private List<Map<String, Object>> eventSchedule;
}
