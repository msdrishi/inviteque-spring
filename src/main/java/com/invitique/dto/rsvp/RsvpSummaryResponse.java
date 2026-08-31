package com.invitique.dto.rsvp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsvpSummaryResponse {
    private long totalResponses;
    private long attending;
    private long declined;
    private long maybe;
    private long totalAttendingGuests;
    private long totalPossibleGuests;
    private List<EventCountDto> eventCounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventCountDto {
        private UUID eventId;
        private String eventName;
        private String eventDate;
        private String eventTime;
        private long attending;
        private long declined;
        private long maybe;
    }
}
