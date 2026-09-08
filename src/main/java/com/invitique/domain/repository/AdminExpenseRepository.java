package com.invitique.domain.repository;

import com.invitique.domain.model.AdminExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminExpenseRepository extends JpaRepository<AdminExpense, String> {
}
