package com.invitique.domain.repository;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.Rsvp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RsvpRepository extends JpaRepository<Rsvp, UUID> {
    List<Rsvp> findByWeddingOrderBySubmittedAtDesc(Invite wedding);
    Page<Rsvp> findByWeddingOrderBySubmittedAtDesc(Invite wedding, Pageable pageable);
    
    Optional<Rsvp> findByWeddingAndIdempotencyKey(Invite wedding, String idempotencyKey);

    @Query("SELECT r FROM Rsvp r LEFT JOIN r.guestGroup g WHERE r.wedding.id = :weddingId " +
           "AND (:status IS NULL OR LOWER(r.attendanceStatus) = LOWER(:status)) " +
           "AND (:groupId IS NULL OR g.id = :groupId) " +
           "AND (:search IS NULL OR LOWER(r.guestName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(r.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.submittedAt DESC")
    Page<Rsvp> findFilteredRsvps(
            @Param("weddingId") UUID weddingId,
            @Param("status") String status,
            @Param("groupId") UUID groupId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM Rsvp r WHERE r.wedding.id = :weddingId")
    long countTotalByWeddingId(@Param("weddingId") UUID weddingId);

    @Query("SELECT COUNT(r) FROM Rsvp r WHERE r.wedding.id = :weddingId AND LOWER(r.attendanceStatus) = LOWER(:status)")
    long countByWeddingIdAndStatus(@Param("weddingId") UUID weddingId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(r.guestCount), 0) FROM Rsvp r WHERE r.wedding.id = :weddingId AND LOWER(r.attendanceStatus) = 'yes'")
    long sumAttendingGuestsByWeddingId(@Param("weddingId") UUID weddingId);

    @Query("SELECT COALESCE(SUM(r.guestCount), 0) FROM Rsvp r WHERE r.wedding.id = :weddingId")
    long sumTotalGuestsByWeddingId(@Param("weddingId") UUID weddingId);
}
