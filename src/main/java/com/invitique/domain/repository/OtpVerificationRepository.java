package com.invitique.domain.repository;

import com.invitique.domain.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    
    Optional<OtpVerification> findFirstByPhoneNumberAndVerifiedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
            String phoneNumber, LocalDateTime now);
}
