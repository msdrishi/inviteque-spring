package com.invitique.dto.rsvp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RsvpSubmissionResponse {
    private boolean success;
    private String message;
    private UUID rsvpId;
    private String guestName;
    private String attendance;
}
