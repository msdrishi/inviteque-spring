package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visitor_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VisitorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 512)
    private String path;

    @Column(name = "template_id", length = 100)
    private String templateId;

    @Column(name = "invite_code", length = 50)
    private String inviteCode;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "visited_at", updatable = false)
    private LocalDateTime visitedAt;
}
