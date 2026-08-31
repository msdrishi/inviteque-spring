package com.invitique.domain.repository;

import com.invitique.domain.model.GuestGroup;
import com.invitique.domain.model.GuestGroupEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestGroupEventRepository extends JpaRepository<GuestGroupEvent, UUID> {
    List<GuestGroupEvent> findByGuestGroup(GuestGroup guestGroup);
    List<GuestGroupEvent> findByGuestGroupId(UUID guestGroupId);
    void deleteByGuestGroupId(UUID guestGroupId);
}
