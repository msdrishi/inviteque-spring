package com.invitique.domain.repository;

import com.invitique.domain.model.Rsvp;
import com.invitique.domain.model.RsvpEventResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RsvpEventResponseRepository extends JpaRepository<RsvpEventResponse, UUID> {
    List<RsvpEventResponse> findByRsvp(Rsvp rsvp);
    List<RsvpEventResponse> findByRsvpId(UUID rsvpId);
    List<RsvpEventResponse> findByEventId(UUID eventId);

    @Query("SELECT rer FROM RsvpEventResponse rer WHERE rer.rsvp.id IN :rsvpIds")
    List<RsvpEventResponse> findByRsvpIds(@Param("rsvpIds") List<UUID> rsvpIds);

    @Query("SELECT COUNT(rer) FROM RsvpEventResponse rer WHERE rer.event.id = :eventId AND LOWER(rer.response) = 'yes'")
    long countAttendingByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(rer) FROM RsvpEventResponse rer WHERE rer.event.id = :eventId AND LOWER(rer.response) = 'no'")
    long countDeclinedByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(rer) FROM RsvpEventResponse rer WHERE rer.event.id = :eventId AND LOWER(rer.response) = 'maybe'")
    long countMaybeByEventId(@Param("eventId") UUID eventId);
}
