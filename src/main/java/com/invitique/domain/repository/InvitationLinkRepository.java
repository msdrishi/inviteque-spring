package com.invitique.domain.repository;

import com.invitique.domain.model.InvitationLink;
import com.invitique.domain.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationLinkRepository extends JpaRepository<InvitationLink, UUID> {
    List<InvitationLink> findByWedding(Invite wedding);
    List<InvitationLink> findByWeddingId(UUID weddingId);
    Optional<InvitationLink> findByWeddingAndSlug(Invite wedding, String slug);
    Optional<InvitationLink> findByWeddingIdAndSlug(UUID weddingId, String slug);
}
