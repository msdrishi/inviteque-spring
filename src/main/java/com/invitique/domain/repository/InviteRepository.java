package com.invitique.domain.repository;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {
    Optional<Invite> findByCode(String code);
    Optional<Invite> findBySlug(String slug);
    Optional<Invite> findBySlugIgnoreCase(String slug);
    List<Invite> findByUser(User user);
    List<Invite> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByCode(String code);
    boolean existsBySlug(String slug);
}
