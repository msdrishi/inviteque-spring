package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settlements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminSettlement {
    @Id
    private String id;

    private String clientName;
    private String serviceType;
    private Double amount;
    private String paymentMethod;
    
    @Column(name = "settlement_date")
    private String date;
    
    private String status;
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
