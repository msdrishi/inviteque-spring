package com.invitique.domain.repository;

import com.invitique.domain.model.GuestGroup;
import com.invitique.domain.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuestGroupRepository extends JpaRepository<GuestGroup, UUID> {
    List<GuestGroup> findByWedding(Invite wedding);
    List<GuestGroup> findByWeddingId(UUID weddingId);
    Optional<GuestGroup> findByWeddingAndSlug(Invite wedding, String slug);
    Optional<GuestGroup> findByWeddingIdAndSlug(UUID weddingId, String slug);
}
