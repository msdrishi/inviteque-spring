package com.invitique.domain.repository;

import com.invitique.domain.model.AdminClientOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminClientOrderRepository extends JpaRepository<AdminClientOrder, String> {
}
