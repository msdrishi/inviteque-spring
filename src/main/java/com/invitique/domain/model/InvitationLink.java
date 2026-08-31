package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invitation_links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvitationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false)
    private Invite wedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_group_id")
    private GuestGroup guestGroup;

    @Column(nullable = false, length = 100)
    private String slug;

    private String label;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
