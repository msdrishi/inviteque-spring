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
@Table(name = "invites")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "template_id", nullable = false)
    private String templateId;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InviteStatus status = InviteStatus.DRAFT;

    // JSONB columns for flexible wedding data
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "couple_data", columnDefinition = "jsonb")
    private Map<String, Object> coupleData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hero_data", columnDefinition = "jsonb")
    private Map<String, Object> heroData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "story_data", columnDefinition = "jsonb")
    private Map<String, Object> storyData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "invitation_data", columnDefinition = "jsonb")
    private Map<String, Object> invitationData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "venue_data", columnDefinition = "jsonb")
    private Map<String, Object> venueData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_data", columnDefinition = "jsonb")
    private Map<String, Object> scheduleData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rsvp_data", columnDefinition = "jsonb")
    private Map<String, Object> rsvpData;

    // Payment info
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "amount_paid")
    private Double amountPaid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "coupon_code")
    private String couponCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum InviteStatus {
        DRAFT, PAID
    }
}
