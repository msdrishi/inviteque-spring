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
public class GuestGroupRequest {
    private String name;
    private String slug;
    private List<UUID> allowedEventIds;
}
