package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rsvp_event_responses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RsvpEventResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rsvp_id", nullable = false)
    private Rsvp rsvp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private WeddingEvent event;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String response = "yes";
}
