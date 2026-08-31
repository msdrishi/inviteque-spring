package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "rsvps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rsvp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false)
    private Invite wedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_group_id")
    private GuestGroup guestGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_link_id")
    private InvitationLink invitationLink;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Builder.Default
    @Column(name = "attendance_status", nullable = false, length = 20)
    private String attendanceStatus = "yes";

    @Builder.Default
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount = 1;

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "meal_preference", length = 100)
    private String mealPreference;

    @Builder.Default
    @Column(name = "accommodation_needed")
    private Boolean accommodationNeeded = false;

    @Column(name = "dietary_notes", columnDefinition = "text")
    private String dietaryNotes;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_responses", columnDefinition = "jsonb")
    private Map<String, Object> customResponses;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
