package com.invitique.service.impl;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.dto.request.InviteRequest;
import com.invitique.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // Removed similar looking chars like 0, 1, O, I
    private final Random random = new Random();

    @Override
    @Transactional
    public Invite createOrUpdateInvite(User user, InviteRequest request) {
        Invite invite;
        
        // Check if updating an existing invite
        if (request.getCode() != null) {
            invite = inviteRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException("Invite not found with code: " + request.getCode()));
            
            // Security check: Ensure the invite belongs to the user
            if (!invite.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this invite");
            }
        } else {
            // Create new invite
            invite = new Invite();
            invite.setUser(user);
            invite.setCode(generateUniqueCode());
        }

        if (request.getStatus() != null) {
            try {
                invite.setStatus(Invite.InviteStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                // Default to DRAFT if invalid status
                invite.setStatus(Invite.InviteStatus.DRAFT);
            }
        } else if (invite.getStatus() == null) {
            invite.setStatus(Invite.InviteStatus.DRAFT);
        }

        invite.setTemplateId(request.getTemplateId());
        invite.setCoupleData(request.getCoupleData());
        invite.setHeroData(request.getHeroData());
        invite.setStoryData(request.getStoryData());
        invite.setInvitationData(request.getInvitationData());
        invite.setEventData(request.getEventData());
        invite.setVenueData(request.getVenueData());
        invite.setScheduleData(request.getScheduleData());
        invite.setRsvpData(request.getRsvpData());

        return inviteRepository.save(invite);
    }

    @Override
    public Optional<Invite> getInviteByCode(String code) {
        return inviteRepository.findByCode(code.toUpperCase());
    }

    @Override
    public Optional<Invite> getInviteById(UUID id) {
        return inviteRepository.findById(id);
    }

    @Override
    public List<Invite> getUserInvites(User user) {
        return inviteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(UUID inviteId, String orderId, String paymentId, int amount) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        invite.setStatus(Invite.InviteStatus.PAID);
        invite.setRazorpayOrderId(orderId);
        invite.setRazorpayPaymentId(paymentId);
        invite.setAmountPaid(amount);
        invite.setPaidAt(LocalDateTime.now());
        
        inviteRepository.save(invite);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (inviteRepository.existsByCode(code));
        return code;
    }
}
