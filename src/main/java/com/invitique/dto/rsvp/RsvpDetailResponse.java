package com.invitique.dto.rsvp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsvpDetailResponse {
    private UUID id;
    private UUID weddingId;
    private String weddingCode;
    private String guestName;
    private String attendanceStatus;
    private Integer guestCount;
    private String message;
    private String mealPreference;
    private Boolean accommodationNeeded;
    private String dietaryNotes;
    private String guestGroupName;
    private String guestGroupSlug;
    private String invitationLinkSlug;
    private LocalDateTime submittedAt;
    private List<EventResponseDetail> eventResponses;
    private Map<String, Object> customResponses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventResponseDetail {
        private UUID eventId;
        private String eventName;
        private String eventDate;
        private String eventTime;
        private String venue;
        private String response;
    }
}
