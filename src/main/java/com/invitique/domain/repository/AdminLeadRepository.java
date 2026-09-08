package com.invitique.domain.repository;

import com.invitique.domain.model.AdminLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLeadRepository extends JpaRepository<AdminLead, String> {
}
