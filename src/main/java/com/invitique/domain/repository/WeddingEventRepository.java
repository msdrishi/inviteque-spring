package com.invitique.domain.repository;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.WeddingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeddingEventRepository extends JpaRepository<WeddingEvent, UUID> {
    List<WeddingEvent> findByWeddingOrderBySortOrderAscCreatedAtAsc(Invite wedding);
    List<WeddingEvent> findByWeddingIdOrderBySortOrderAscCreatedAtAsc(UUID weddingId);
}
