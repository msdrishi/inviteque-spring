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
}
