package com.invitique.web.controller;

import com.invitique.dto.rsvp.RsvpPublicConfigResponse;
import com.invitique.dto.rsvp.RsvpSubmissionRequest;
import com.invitique.dto.rsvp.RsvpSubmissionResponse;
import com.invitique.service.RsvpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/rsvp")
@RequiredArgsConstructor
public class RsvpPublicController {

    private final RsvpService rsvpService;

    @PostMapping
    public ResponseEntity<RsvpSubmissionResponse> submitRsvp(@RequestBody RsvpSubmissionRequest request) {
        RsvpSubmissionResponse response = rsvpService.submitRsvp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/config/{code}")
    public ResponseEntity<RsvpPublicConfigResponse> getPublicRsvpConfig(
            @PathVariable String code,
            @RequestParam(required = false) String group) {
        RsvpPublicConfigResponse config = rsvpService.getPublicRsvpConfig(code, group);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/weddings/{code}/rsvp-summary")
    public ResponseEntity<com.invitique.dto.rsvp.RsvpSummaryResponse> getPublicRsvpSummary(@PathVariable String code) {
        com.invitique.dto.rsvp.RsvpSummaryResponse summary = rsvpService.getPublicRsvpSummary(code);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/weddings/{code}/rsvps")
    public ResponseEntity<java.util.List<com.invitique.dto.rsvp.RsvpDetailResponse>> getPublicRsvps(@PathVariable String code) {
        java.util.List<com.invitique.dto.rsvp.RsvpDetailResponse> responses = rsvpService.getPublicRsvps(code);
        return ResponseEntity.ok(responses);
    }

    @GetMapping(value = "/weddings/{code}/rsvps/export", produces = "text/csv")
    public ResponseEntity<String> exportRsvpsCsv(@PathVariable String code) {
        String csvData = rsvpService.getPublicExportRsvpsCsv(code);
        return ResponseEntity.ok(csvData);
    }
}