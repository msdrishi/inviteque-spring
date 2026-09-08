package com.invitique.domain.repository;

import com.invitique.domain.model.AdminSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSettlementRepository extends JpaRepository<AdminSettlement, String> {
}
