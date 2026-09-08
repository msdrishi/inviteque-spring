package com.invitique.domain.repository;

import com.invitique.domain.model.AdminTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminTodoRepository extends JpaRepository<AdminTodo, String> {
}
