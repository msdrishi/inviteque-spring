package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminExpense {
    @Id
    private String id;

    private String title;
    private String category;
    private Double amount;
    private String frequency;
    private String paymentMethod;
    
    @Column(name = "expense_date")
    private String date;
    
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
