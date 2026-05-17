package com.invitique.web.controller;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.dto.request.InviteRequest;
import com.invitique.dto.response.InviteResponse;
import com.invitique.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(invite));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllInvites(
            @AuthenticationPrincipal User user) {
        List<InviteResponse> responses = inviteService.getUserInvites(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "invitations", responses,
                "total", responses.size()
        ));
    }

    @GetMapping("/{idOrCode}")
    public ResponseEntity<InviteResponse> getInvite(@PathVariable String idOrCode) {
        Optional<Invite> inviteOpt;
        try {
            UUID uuid = UUID.fromString(idOrCode);
            inviteOpt = inviteService.getInviteById(uuid);
        } catch (IllegalArgumentException e) {
            inviteOpt = inviteService.getInviteByCode(idOrCode);
        }
        
        return inviteOpt.map(invite -> ResponseEntity.ok(mapToResponse(invite)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InviteResponse> updateInvite(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestBody InviteRequest request) {
        Invite invite = inviteService.updateInvite(user, id, request);
        return ResponseEntity.ok(mapToResponse(invite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteInvite(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        inviteService.deleteInvite(user, id);
        return ResponseEntity.ok(Map.of("success", true));
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

    @SuppressWarnings("unchecked")
    private InviteResponse mapToResponse(Invite invite) {
        String groom = invite.getCoupleData() != null ? (String) invite.getCoupleData().get("groomName") : null;
        String bride = invite.getCoupleData() != null ? (String) invite.getCoupleData().get("brideName") : null;
        String coupleNames = (groom != null && bride != null) ? groom + " & " + bride : null;
        
        String mahal = invite.getVenueData() != null ? (String) invite.getVenueData().get("mahalName") : null;
        String city = invite.getVenueData() != null ? (String) invite.getVenueData().get("venueCity") : null;
        String address = invite.getVenueData() != null ? (String) invite.getVenueData().get("venueAddress") : null;
        
        Map<String, String> dateMap = null;
        if (invite.getHeroData() != null) {
            dateMap = new java.util.HashMap<>();
            dateMap.put("day", (String) invite.getHeroData().get("weddingDate"));
            dateMap.put("month", (String) invite.getHeroData().get("weddingMonth"));
            dateMap.put("year", (String) invite.getHeroData().get("weddingYear"));
        }
        
        List<String> photosList = null;
        if (invite.getStoryData() != null) {
            Object photosObj = invite.getStoryData().get("photos");
            if (photosObj instanceof List) {
                photosList = (List<String>) photosObj;
            }
        }
        
        List<Map<String, Object>> scheduleList = null;
        if (invite.getScheduleData() != null) {
            Object itemsObj = invite.getScheduleData().get("items");
            if (itemsObj instanceof List) {
                scheduleList = (List<Map<String, Object>>) itemsObj;
            }
        }

        return InviteResponse.builder()
                .id(invite.getId())
                .inviteId(invite.getId())
                .success(true)
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
                .coupleNames(coupleNames)
                .groomName(groom)
                .brideName(bride)
                .mahalName(mahal)
                .weddingDate(dateMap)
                .venueCity(city)
                .venueName(address)
                .photos(photosList)
                .eventSchedule(scheduleList)
                .build();
    }
}
