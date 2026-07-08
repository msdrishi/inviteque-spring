package com.invitique.service.impl;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.domain.repository.CouponRepository;
import com.invitique.dto.request.InviteRequest;
import com.invitique.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;
    private final CouponRepository couponRepository;
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // Removed similar looking chars like 0, 1, O, I
    private final Random random = new Random();

    @Override
    @Transactional
    public Invite createOrUpdateInvite(User user, InviteRequest request) {
        Invite invite;
        
        // Check if updating an existing invite by code or id
        if (request.getCode() != null) {
            invite = inviteRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException("Invite not found with code: " + request.getCode()));
            
            // Security check: Ensure the invite belongs to the user
            if (!invite.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this invite");
            }
        } else if (request.getId() != null) {
            UUID id;
            try {
                id = UUID.fromString(request.getId());
            } catch (IllegalArgumentException e) {
                id = null;
            }
            if (id != null) {
                invite = inviteRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Invite not found with id: " + request.getId()));
                
                // Security check
                if (!invite.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("Unauthorized to update this invite");
                }
            } else {
                invite = new Invite();
                invite.setUser(user);
                invite.setCode(generateUniqueCode());
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
                invite.setStatus(Invite.InviteStatus.DRAFT);
            }
        } else if (invite.getStatus() == null) {
            invite.setStatus(Invite.InviteStatus.DRAFT);
        }

        invite.setTemplateId(request.getTemplateId());

        // Dynamic flat fields mapping vs JSONB maps
        if (request.getGroomName() != null || request.getBrideName() != null || request.getPhotos() != null || request.getEventSchedule() != null || request.getMahalName() != null) {
            // Populate coupleData
            Map<String, Object> coupleData = new java.util.HashMap<>();
            coupleData.put("groomName", request.getGroomName());
            coupleData.put("brideName", request.getBrideName());
            invite.setCoupleData(coupleData);

            // Populate heroData
            Map<String, Object> heroData = new java.util.HashMap<>();
            heroData.put("groomName", request.getGroomName());
            heroData.put("brideName", request.getBrideName());
            if (request.getWeddingDate() != null) {
                if (request.getWeddingDate() instanceof Map) {
                    Map<?, ?> dateMap = (Map<?, ?>) request.getWeddingDate();
                    heroData.put("weddingDate", dateMap.get("day"));
                    heroData.put("weddingMonth", dateMap.get("month"));
                    heroData.put("weddingYear", dateMap.get("year"));
                } else if (request.getWeddingDate() instanceof String) {
                    heroData.put("weddingDate", request.getWeddingDate());
                }
            }
            if (request.getWeddingTime() != null) {
                heroData.put("weddingTime", request.getWeddingTime());
            }
            invite.setHeroData(heroData);

            // Populate venueData
            Map<String, Object> venueData = new java.util.HashMap<>();
            venueData.put("mahalName", request.getMahalName());
            venueData.put("venueAddress", request.getVenueName());
            venueData.put("venueCity", request.getVenueCity());
            venueData.put("state", request.getState());
            venueData.put("mapLink", request.getMapLink());
            invite.setVenueData(venueData);

            // Populate storyData
            Map<String, Object> storyData = new java.util.HashMap<>();
            storyData.put("photos", request.getPhotos());
            invite.setStoryData(storyData);

            // Populate scheduleData
            Map<String, Object> scheduleData = new java.util.HashMap<>();
            scheduleData.put("showSchedule", true);
            scheduleData.put("showGallery", true);
            scheduleData.put("items", request.getEventSchedule());
            invite.setScheduleData(scheduleData);
        } else {
            // Fallback to JSONB maps if flat fields are not present
            if (request.getCoupleData() != null) invite.setCoupleData(request.getCoupleData());
            if (request.getHeroData() != null) invite.setHeroData(request.getHeroData());
            if (request.getVenueData() != null) invite.setVenueData(request.getVenueData());
            if (request.getStoryData() != null) invite.setStoryData(request.getStoryData());
            if (request.getScheduleData() != null) invite.setScheduleData(request.getScheduleData());
        }

        if (request.getInvitationData() != null) invite.setInvitationData(request.getInvitationData());
        if (request.getEventData() != null) invite.setEventData(request.getEventData());
        if (request.getRsvpData() != null) invite.setRsvpData(request.getRsvpData());
        if (request.getCouponCode() != null) invite.setCouponCode(request.getCouponCode());

        Invite savedInvite = inviteRepository.save(invite);

        if (savedInvite.getStatus() == Invite.InviteStatus.PAID && savedInvite.getCouponCode() != null) {
            claimCoupon(savedInvite.getCouponCode(), savedInvite);
        }

        return savedInvite;
    }

    @Override
    @Transactional
    public Invite updateInvite(User user, UUID id, InviteRequest request) {
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invite not found with id: " + id));
        
        // Security check
        if (!invite.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this invite");
        }

        if (request.getTemplateId() != null) {
            invite.setTemplateId(request.getTemplateId());
        }

        if (request.getStatus() != null) {
            try {
                invite.setStatus(Invite.InviteStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        // Apply partial updates: if new flat fields are sent, merge them into the JSONB structures
        if (request.getGroomName() != null || request.getBrideName() != null || request.getPhotos() != null || request.getEventSchedule() != null || request.getMahalName() != null || request.getVenueCity() != null || request.getVenueName() != null || request.getWeddingDate() != null || request.getWeddingTime() != null || request.getState() != null || request.getMapLink() != null) {
            
            // Merge coupleData
            Map<String, Object> coupleData = invite.getCoupleData() != null ? new java.util.HashMap<>(invite.getCoupleData()) : new java.util.HashMap<>();
            if (request.getGroomName() != null) coupleData.put("groomName", request.getGroomName());
            if (request.getBrideName() != null) coupleData.put("brideName", request.getBrideName());
            invite.setCoupleData(coupleData);

            // Merge heroData
            Map<String, Object> heroData = invite.getHeroData() != null ? new java.util.HashMap<>(invite.getHeroData()) : new java.util.HashMap<>();
            if (request.getGroomName() != null) heroData.put("groomName", request.getGroomName());
            if (request.getBrideName() != null) heroData.put("brideName", request.getBrideName());
            if (request.getWeddingDate() != null) {
                if (request.getWeddingDate() instanceof Map) {
                    Map<?, ?> dateMap = (Map<?, ?>) request.getWeddingDate();
                    if (dateMap.get("day") != null) heroData.put("weddingDate", dateMap.get("day"));
                    if (dateMap.get("month") != null) heroData.put("weddingMonth", dateMap.get("month"));
                    if (dateMap.get("year") != null) heroData.put("weddingYear", dateMap.get("year"));
                } else if (request.getWeddingDate() instanceof String) {
                    heroData.put("weddingDate", request.getWeddingDate());
                }
            }
            if (request.getWeddingTime() != null) {
                heroData.put("weddingTime", request.getWeddingTime());
            }
            invite.setHeroData(heroData);

            // Merge venueData
            Map<String, Object> venueData = invite.getVenueData() != null ? new java.util.HashMap<>(invite.getVenueData()) : new java.util.HashMap<>();
            if (request.getMahalName() != null) venueData.put("mahalName", request.getMahalName());
            if (request.getVenueName() != null) venueData.put("venueAddress", request.getVenueName());
            if (request.getVenueCity() != null) venueData.put("venueCity", request.getVenueCity());
            if (request.getState() != null) venueData.put("state", request.getState());
            if (request.getMapLink() != null) venueData.put("mapLink", request.getMapLink());
            invite.setVenueData(venueData);

            // Merge storyData
            Map<String, Object> storyData = invite.getStoryData() != null ? new java.util.HashMap<>(invite.getStoryData()) : new java.util.HashMap<>();
            if (request.getPhotos() != null) storyData.put("photos", request.getPhotos());
            invite.setStoryData(storyData);

            // Merge scheduleData
            Map<String, Object> scheduleData = invite.getScheduleData() != null ? new java.util.HashMap<>(invite.getScheduleData()) : new java.util.HashMap<>();
            if (request.getEventSchedule() != null) scheduleData.put("items", request.getEventSchedule());
            invite.setScheduleData(scheduleData);
        } else {
            // Fallback: merge JSONB maps
            if (request.getCoupleData() != null) {
                Map<String, Object> couple = invite.getCoupleData() != null ? new java.util.HashMap<>(invite.getCoupleData()) : new java.util.HashMap<>();
                couple.putAll(request.getCoupleData());
                invite.setCoupleData(couple);
            }
            if (request.getHeroData() != null) {
                Map<String, Object> hero = invite.getHeroData() != null ? new java.util.HashMap<>(invite.getHeroData()) : new java.util.HashMap<>();
                hero.putAll(request.getHeroData());
                invite.setHeroData(hero);
            }
            if (request.getVenueData() != null) {
                Map<String, Object> venue = invite.getVenueData() != null ? new java.util.HashMap<>(invite.getVenueData()) : new java.util.HashMap<>();
                venue.putAll(request.getVenueData());
                invite.setVenueData(venue);
            }
            if (request.getStoryData() != null) {
                Map<String, Object> story = invite.getStoryData() != null ? new java.util.HashMap<>(invite.getStoryData()) : new java.util.HashMap<>();
                story.putAll(request.getStoryData());
                invite.setStoryData(story);
            }
            if (request.getScheduleData() != null) {
                Map<String, Object> schedule = invite.getScheduleData() != null ? new java.util.HashMap<>(invite.getScheduleData()) : new java.util.HashMap<>();
                schedule.putAll(request.getScheduleData());
                invite.setScheduleData(schedule);
            }
        }

        if (request.getInvitationData() != null) {
            Map<String, Object> inv = invite.getInvitationData() != null ? new java.util.HashMap<>(invite.getInvitationData()) : new java.util.HashMap<>();
            inv.putAll(request.getInvitationData());
            invite.setInvitationData(inv);
        }
        if (request.getEventData() != null) {
            Map<String, Object> ev = invite.getEventData() != null ? new java.util.HashMap<>(invite.getEventData()) : new java.util.HashMap<>();
            ev.putAll(request.getEventData());
            invite.setEventData(ev);
        }
        if (request.getRsvpData() != null) {
            Map<String, Object> rsvp = invite.getRsvpData() != null ? new java.util.HashMap<>(invite.getRsvpData()) : new java.util.HashMap<>();
            rsvp.putAll(request.getRsvpData());
            invite.setRsvpData(rsvp);
        }

        return inviteRepository.save(invite);
    }

    @Override
    @Transactional
    public void deleteInvite(User user, UUID id) {
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invite not found with id: " + id));
        
        // Security check
        if (!invite.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this invite");
        }
        
        inviteRepository.delete(invite);
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
    public void updatePaymentStatus(UUID inviteId, String orderId, String paymentId, double amount) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        
        invite.setStatus(Invite.InviteStatus.PAID);
        invite.setRazorpayOrderId(orderId);
        invite.setRazorpayPaymentId(paymentId);
        invite.setAmountPaid(amount);
        invite.setPaidAt(LocalDateTime.now());
        
        Invite savedInvite = inviteRepository.save(invite);

        if (savedInvite.getCouponCode() != null) {
            claimCoupon(savedInvite.getCouponCode(), savedInvite);
        }
    }

    private void claimCoupon(String couponCode, Invite invite) {
        couponRepository.findByCodeIgnoreCase(couponCode).ifPresent(coupon -> {
            if (coupon.isAvailable()) {
                coupon.setAvailable(false);
                coupon.setPurchasedDate(LocalDateTime.now());
                coupon.setInviteId(invite.getId());
                couponRepository.save(coupon);
            }
        });
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
