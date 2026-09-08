package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "admin_client_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminClientOrder {
    @Id
    private String id;

    private String clientName;
    private String phone;
    private String email;
    private String source;
    private String serviceName;
    private Double totalCharge;
    private Double advancePaid;
    private String advanceDate;
    private String clientDeadline;
    private String deliveryDate;
    private String status;
    private String deliverableUrl;

    @ElementCollection
    @CollectionTable(name = "admin_client_order_urls", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "url")
    private List<String> deliverableUrls;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
