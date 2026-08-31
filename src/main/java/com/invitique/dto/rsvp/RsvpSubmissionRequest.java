package com.invitique.dto.rsvp;

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
public class RsvpSubmissionRequest {
    private String weddingCode;
    private String invitationLink;
    private String guestName;
    private String attendance;
    private Integer guestCount;
    private String message;
    private String mealPreference;
    private Boolean accommodationNeeded;
    private String dietaryNotes;
    private String idempotencyKey;
    private List<EventResponseItem> events;
    private Map<String, Object> customResponses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventResponseItem {
        private UUID eventId;
        private String eventName;
        private String response;
    }
}
