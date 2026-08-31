package com.invitique.service;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.dto.rsvp.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RsvpService {
    RsvpSubmissionResponse submitRsvp(RsvpSubmissionRequest request);
    RsvpPublicConfigResponse getPublicRsvpConfig(String code, String groupSlug);
    RsvpSummaryResponse getRsvpSummary(User user, String code);
    Page<RsvpDetailResponse> getRsvps(User user, String code, String status, UUID groupId, String search, Pageable pageable);
    RsvpDetailResponse getRsvpById(User user, String code, UUID rsvpId);
    String exportRsvpsCsv(User user, String code);
    GuestGroupDto createGuestGroup(User user, String code, GuestGroupRequest request);
    List<GuestGroupDto> getGuestGroups(User user, String code);
    void syncEventsFromScheduleData(Invite invite);
}
