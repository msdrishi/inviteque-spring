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
public class RsvpPublicConfigResponse {
    private boolean enabled;
    private String title;
    private String description;
    private String successTitle;
    private String successMessage;
    private boolean allowMaybe;
    private boolean allowGuestCount;
    private boolean allowEventSelection;
    private boolean allowMessage;
    private boolean allowMealPreference;
    private boolean allowAccommodation;
    private Integer maxGuestsPerSubmission;
    private String groupName;
    private String groupSlug;
    private List<EventConfigDto> events;
    private Map<String, Object> customQuestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventConfigDto {
        private UUID id;
        private String name;
        private String date;
        private String time;
        private String venue;
        private String address;
        private Integer sortOrder;
    }
}
