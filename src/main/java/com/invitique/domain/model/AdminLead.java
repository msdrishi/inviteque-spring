package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_leads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminLead {
    @Id
    private String id;

    private String name;
    private String phone;
    private String source;
    private String serviceInterested;
    private Double budgetExpectation;
    private String inquiryDate;
    private String status;
    
    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
