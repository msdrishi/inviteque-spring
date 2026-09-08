package com.invitique.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_todos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminTodo {
    @Id
    private String id;

    @Column(length = 500)
    private String text;
    private String priority;
    private String tag;
    private String dueDate;
    private Boolean completed;

    @Column(name = "todo_created_at")
    private String createdAtDateString; // to match front-end "createdAt" string field if needed

    @CreationTimestamp
    @Column(name = "db_created_at", updatable = false)
    private LocalDateTime dbCreatedAt;

    @UpdateTimestamp
    @Column(name = "db_updated_at")
    private LocalDateTime dbUpdatedAt;
}
