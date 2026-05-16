package com.invitique.web.controller;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.dto.request.InviteRequest;
import com.invitique.dto.response.InviteResponse;
import com.invitique.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @PostMapping
    public ResponseEntity<InviteResponse> createOrUpdateInvite(
            @AuthenticationPrincipal User user,
            @RequestBody InviteRequest request) {
        Invite invite = inviteService.createOrUpdateInvite(user, request);
        return ResponseEntity.ok(mapToResponse(invite));
    }

    @GetMapping("/{code}")
    public ResponseEntity<InviteResponse> getInviteByCode(@PathVariable String code) {
        return inviteService.getInviteByCode(code)
                .map(invite -> ResponseEntity.ok(mapToResponse(invite)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<InviteResponse>> getMyInvites(@AuthenticationPrincipal User user) {
        List<InviteResponse> responses = inviteService.getUserInvites(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<InviteResponse> getInviteById(@PathVariable UUID id) {
        return inviteService.getInviteById(id)
                .map(invite -> ResponseEntity.ok(mapToResponse(invite)))
                .orElse(ResponseEntity.notFound().build());
    }

    private InviteResponse mapToResponse(Invite invite) {
        return InviteResponse.builder()
                .id(invite.getId())
                .templateId(invite.getTemplateId())
                .code(invite.getCode())
                .status(invite.getStatus().name())
                .coupleData(invite.getCoupleData())
                .heroData(invite.getHeroData())
                .storyData(invite.getStoryData())
                .invitationData(invite.getInvitationData())
                .eventData(invite.getEventData())
                .venueData(invite.getVenueData())
                .scheduleData(invite.getScheduleData())
                .rsvpData(invite.getRsvpData())
                .createdAt(invite.getCreatedAt())
                .build();
    }
}
