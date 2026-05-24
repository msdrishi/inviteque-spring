package com.invitique.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteRequest {
    private String id; // Optional ID for updates
    private String code; // Optional unique code for updates
    private String templateId;
    private Map<String, Object> coupleData;
    private Map<String, Object> heroData;
    private Map<String, Object> storyData;
    private Map<String, Object> invitationData;
    private Map<String, Object> eventData;
    private Map<String, Object> venueData;
    private Map<String, Object> scheduleData;
    private Map<String, Object> rsvpData;
    private String status;
    private String couponCode;

    // Flat fields for Cloudinary and Modern Builder integration
    private String coupleNames;
    private String groomName;
    private String brideName;
    private String mahalName;
    private Object weddingDate; // Can be Map with day/month/year or a String
    private String venueCity;
    private String venueName;
    private List<String> photos;
    private List<Map<String, Object>> eventSchedule;
}
