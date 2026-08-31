package com.invitique.web.controller;

import com.invitique.domain.model.User;
import com.invitique.dto.rsvp.*;
import com.invitique.service.RsvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/weddings/{code}")
@RequiredArgsConstructor
public class RsvpDashboardController {

    private final RsvpService rsvpService;

    @GetMapping("/rsvps")
    public ResponseEntity<Page<RsvpDetailResponse>> getRsvps(
            @AuthenticationPrincipal User user,
            @PathVariable String code,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<RsvpDetailResponse> responses = rsvpService.getRsvps(
                user, code, status, groupId, search,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"))
        );
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/rsvp-summary")
    public ResponseEntity<RsvpSummaryResponse> getRsvpSummary(
            @AuthenticationPrincipal User user,
            @PathVariable String code) {
        RsvpSummaryResponse summary = rsvpService.getRsvpSummary(user, code);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/rsvps/{rsvpId}")
    public ResponseEntity<RsvpDetailResponse> getRsvpById(
            @AuthenticationPrincipal User user,
            @PathVariable String code,
            @PathVariable UUID rsvpId) {
        RsvpDetailResponse detail = rsvpService.getRsvpById(user, code, rsvpId);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/rsvps/export")
    public ResponseEntity<byte[]> exportRsvpsCsv(
            @AuthenticationPrincipal User user,
            @PathVariable String code) {
        String csv = rsvpService.exportRsvpsCsv(user, code);
        byte[] csvBytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rsvp-" + code.toUpperCase() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PostMapping("/guest-groups")
    public ResponseEntity<GuestGroupDto> createGuestGroup(
            @AuthenticationPrincipal User user,
            @PathVariable String code,
            @RequestBody GuestGroupRequest request) {
        GuestGroupDto group = rsvpService.createGuestGroup(user, code, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @GetMapping("/guest-groups")
    public ResponseEntity<List<GuestGroupDto>> getGuestGroups(
            @AuthenticationPrincipal User user,
            @PathVariable String code) {
        List<GuestGroupDto> groups = rsvpService.getGuestGroups(user, code);
        return ResponseEntity.ok(groups);
    }
}
