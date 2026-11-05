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
                .filter(invite -> invite.getStatus() == Invite.InviteStatus.PAID)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "invitations", responses,
                "total", responses.size()
        ));
    }

    /**
     * Public endpoint: fetches invite by UUID, code, or slug.
     * PAID invites are publicly accessible (no auth needed).
     * DRAFT invites require the owner to be authenticated.
     */
    @GetMapping("/{idOrCode}")
    public ResponseEntity<?> getInvite(
            @PathVariable String idOrCode,
            @AuthenticationPrincipal User user) {
        Optional<Invite> inviteOpt;
        try {
            UUID uuid = UUID.fromString(idOrCode);
            inviteOpt = inviteService.getInviteById(uuid);
        } catch (IllegalArgumentException e) {
            // Not a UUID — try by code or slug
            inviteOpt = inviteService.getInviteByCode(idOrCode);
        }

        if (inviteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Invite invite = inviteOpt.get();

        // DRAFT invites: only the owner can view
        if (invite.getStatus() == Invite.InviteStatus.DRAFT) {
            if (user == null || !invite.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only the owner can view draft invitations"));
            }
        }

        return ResponseEntity.ok(mapToResponse(invite));
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
                .filter(invite -> invite.getStatus() == Invite.InviteStatus.PAID)
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

    @GetMapping("/{idOrCode}/features")
    public ResponseEntity<Map<String, Object>> getInviteFeatures(@PathVariable String idOrCode) {
        Optional<Invite> inviteOpt;
        try {
            UUID uuid = UUID.fromString(idOrCode);
            inviteOpt = inviteService.getInviteById(uuid);
        } catch (IllegalArgumentException e) {
            inviteOpt = inviteService.getInviteByCode(idOrCode);
        }

        if (inviteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Invite invite = inviteOpt.get();
        boolean hasRsvp = Boolean.TRUE.equals(invite.getHasRsvp());
        
        boolean showRsvp = true;
        if (invite.getRsvpData() != null && invite.getRsvpData().containsKey("showRsvp")) {
            showRsvp = Boolean.TRUE.equals(invite.getRsvpData().get("showRsvp"));
        } else if (invite.getRsvpData() != null && invite.getRsvpData().containsKey("enabled")) {
            showRsvp = Boolean.TRUE.equals(invite.getRsvpData().get("enabled"));
        }

        boolean showWeddingSchedule = true;
        if (invite.getScheduleData() != null && invite.getScheduleData().containsKey("showWeddingSchedule")) {
            showWeddingSchedule = Boolean.TRUE.equals(invite.getScheduleData().get("showWeddingSchedule"));
        } else if (invite.getScheduleData() != null && invite.getScheduleData().containsKey("showSchedule")) {
            showWeddingSchedule = Boolean.TRUE.equals(invite.getScheduleData().get("showSchedule"));
        }

        return ResponseEntity.ok(Map.of(
                "code", invite.getCode(),
                "hasRsvp", hasRsvp,
                "showRsvp", showRsvp,
                "showWeddingSchedule", showWeddingSchedule,
                "isTemplatePaid", invite.getStatus() == Invite.InviteStatus.PAID
        ));
    }

    @PutMapping("/{idOrCode}/visibility")
    public ResponseEntity<?> updateVisibility(
            @PathVariable String idOrCode,
            @AuthenticationPrincipal User user,
            @RequestBody VisibilityUpdateRequest request) {
        Optional<Invite> inviteOpt;
        try {
            UUID uuid = UUID.fromString(idOrCode);
            inviteOpt = inviteService.getInviteById(uuid);
        } catch (IllegalArgumentException e) {
            inviteOpt = inviteService.getInviteByCode(idOrCode);
        }

        if (inviteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Invite invite = inviteOpt.get();
        if (!invite.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Unauthorized"));
        }

        boolean hasRsvp = Boolean.TRUE.equals(invite.getHasRsvp());

        if (request.getShowRsvp() != null) {
            if (Boolean.TRUE.equals(request.getShowRsvp()) && !hasRsvp) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Cannot enable RSVP visibility without purchasing RSVP entitlement"
                ));
            }
            Map<String, Object> rsvpData = invite.getRsvpData() != null ? new java.util.HashMap<>(invite.getRsvpData()) : new java.util.HashMap<>();
            rsvpData.put("showRsvp", request.getShowRsvp());
            rsvpData.put("enabled", request.getShowRsvp());
            invite.setRsvpData(rsvpData);

            Map<String, Object> invData = invite.getInvitationData() != null ? new java.util.HashMap<>(invite.getInvitationData()) : new java.util.HashMap<>();
            invData.put("showRsvp", request.getShowRsvp());
            invite.setInvitationData(invData);
        }

        if (request.getShowWeddingSchedule() != null) {
            Map<String, Object> schedData = invite.getScheduleData() != null ? new java.util.HashMap<>(invite.getScheduleData()) : new java.util.HashMap<>();
            schedData.put("showSchedule", request.getShowWeddingSchedule());
            schedData.put("showWeddingSchedule", request.getShowWeddingSchedule());
            invite.setScheduleData(schedData);

            Map<String, Object> invData = invite.getInvitationData() != null ? new java.util.HashMap<>(invite.getInvitationData()) : new java.util.HashMap<>();
            invData.put("showSchedule", request.getShowWeddingSchedule());
            invData.put("showWeddingSchedule", request.getShowWeddingSchedule());
            invite.setInvitationData(invData);
        }

        Invite saved = inviteService.updateInvite(user, invite.getId(), InviteRequest.builder()
                .scheduleData(invite.getScheduleData())
                .rsvpData(invite.getRsvpData())
                .invitationData(invite.getInvitationData())
                .build());

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @SuppressWarnings("unchecked")
    private InviteResponse mapToResponse(Invite invite) {
        String groom = invite.getCoupleData() != null ? (String) invite.getCoupleData().get("groomName") : null;
        String bride = invite.getCoupleData() != null ? (String) invite.getCoupleData().get("brideName") : null;
        String coupleNames = (groom != null && bride != null) ? groom + " & " + bride : null;

        String mahal = invite.getVenueData() != null ? (String) invite.getVenueData().get("mahalName") : null;
        String city = invite.getVenueData() != null ? (String) invite.getVenueData().get("venueCity") : null;
        String venueName = invite.getVenueData() != null ? (String) invite.getVenueData().get("venueName") : null;

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

        boolean hasRsvp = Boolean.TRUE.equals(invite.getHasRsvp());
        
        boolean showRsvp = true;
        if (invite.getRsvpData() != null && invite.getRsvpData().containsKey("showRsvp")) {
            showRsvp = Boolean.TRUE.equals(invite.getRsvpData().get("showRsvp"));
        } else if (invite.getRsvpData() != null && invite.getRsvpData().containsKey("enabled")) {
            showRsvp = Boolean.TRUE.equals(invite.getRsvpData().get("enabled"));
        } else if (invite.getInvitationData() != null && invite.getInvitationData().containsKey("showRsvp")) {
            showRsvp = Boolean.TRUE.equals(invite.getInvitationData().get("showRsvp"));
        }

        boolean showWeddingSchedule = true;
        if (invite.getScheduleData() != null && invite.getScheduleData().containsKey("showWeddingSchedule")) {
            showWeddingSchedule = Boolean.TRUE.equals(invite.getScheduleData().get("showWeddingSchedule"));
        } else if (invite.getScheduleData() != null && invite.getScheduleData().containsKey("showSchedule")) {
            showWeddingSchedule = Boolean.TRUE.equals(invite.getScheduleData().get("showSchedule"));
        } else if (invite.getInvitationData() != null && invite.getInvitationData().containsKey("showSchedule")) {
            showWeddingSchedule = Boolean.TRUE.equals(invite.getInvitationData().get("showSchedule"));
        }

        return InviteResponse.builder()
                .id(invite.getId())
                .inviteId(invite.getId())
                .success(true)
                .templateId(invite.getTemplateId())
                .code(invite.getCode())
                .slug(invite.getSlug())
                .status(invite.getStatus().name())
                .hasRsvp(hasRsvp)
                .showRsvp(showRsvp)
                .showWeddingSchedule(showWeddingSchedule)
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
                .venueName(venueName)
                .photos(photosList)
                .eventSchedule(scheduleList)
                .build();
    }

    @lombok.Data
    public static class VisibilityUpdateRequest {
        private Boolean showRsvp;
        private Boolean showWeddingSchedule;
    }
}
