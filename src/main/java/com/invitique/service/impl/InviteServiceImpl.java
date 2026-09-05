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
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private final Random random = new Random();

    @Override
    @Transactional
    public Invite createOrUpdateInvite(User user, InviteRequest request) {
        Invite invite;

        // Priority 1: Look up by slug (human-readable, e.g. "Pavitra-Sri")
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            Optional<Invite> bySlug = inviteRepository.findBySlugIgnoreCase(request.getSlug());
            if (bySlug.isPresent()) {
                invite = bySlug.get();
                // Security: only owner can update
                if (!invite.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("Unauthorized to update this invite");
                }
            } else {
                // Create new invite with this slug
                invite = new Invite();
                invite.setUser(user);
                invite.setCode(generateUniqueCode());
                invite.setSlug(request.getSlug());
            }
        }
        // Priority 2: Look up by code
        else if (request.getCode() != null && !request.getCode().isBlank()) {
            Optional<Invite> byCode = inviteRepository.findByCode(request.getCode().toUpperCase());
            if (byCode.isPresent()) {
                invite = byCode.get();
                if (!invite.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("Unauthorized to update this invite");
                }
            } else {
                // Create new invite
                invite = new Invite();
                invite.setUser(user);
                invite.setCode(generateUniqueCode());
            }
        }
        // Priority 3: Look up by id
        else if (request.getId() != null) {
            UUID id;
            try {
                id = UUID.fromString(request.getId());
            } catch (IllegalArgumentException e) {
                id = null;
            }
            if (id != null) {
                invite = inviteRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Invite not found with id: " + request.getId()));
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

        applyFlatFields(invite, request);

        if (request.getInvitationData() != null) invite.setInvitationData(mergeMap(invite.getInvitationData(), request.getInvitationData()));
        if (request.getEventData() != null) invite.setEventData(mergeMap(invite.getEventData(), request.getEventData()));
        if (request.getRsvpData() != null) invite.setRsvpData(mergeMap(invite.getRsvpData(), request.getRsvpData()));
        if (request.getCouponCode() != null) invite.setCouponCode(request.getCouponCode());

        Invite savedInvite = inviteRepository.save(invite);

        if (savedInvite.getStatus() == Invite.InviteStatus.PAID && savedInvite.getCouponCode() != null) {
            claimCoupon(savedInvite.getCouponCode(), savedInvite);
        }

        return savedInvite;
    }

    private void applyFlatFields(Invite invite, InviteRequest request) {
        boolean hasFlat = request.getGroomName() != null || request.getBrideName() != null
                || request.getPhotos() != null || request.getEventSchedule() != null
                || request.getMahalName() != null || request.getVenueCity() != null
                || request.getVenueName() != null || request.getVenueAddress() != null
                || request.getWeddingDate() != null || request.getWeddingTime() != null
                || request.getState() != null || request.getMapLink() != null
                || request.getHeroSubtitle() != null;

        if (hasFlat) {
            // coupleData
            Map<String, Object> coupleData = invite.getCoupleData() != null ? new java.util.HashMap<>(invite.getCoupleData()) : new java.util.HashMap<>();
            if (request.getGroomName() != null) coupleData.put("groomName", request.getGroomName());
            if (request.getBrideName() != null) coupleData.put("brideName", request.getBrideName());
            invite.setCoupleData(coupleData);

            // heroData
            Map<String, Object> heroData = invite.getHeroData() != null ? new java.util.HashMap<>(invite.getHeroData()) : new java.util.HashMap<>();
            if (request.getGroomName() != null) heroData.put("groomName", request.getGroomName());
            if (request.getBrideName() != null) heroData.put("brideName", request.getBrideName());
            if (request.getHeroSubtitle() != null) heroData.put("heroSubtitle", request.getHeroSubtitle());
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
            if (request.getWeddingTime() != null) heroData.put("weddingTime", request.getWeddingTime());
            invite.setHeroData(heroData);

            // venueData
            Map<String, Object> venueData = invite.getVenueData() != null ? new java.util.HashMap<>(invite.getVenueData()) : new java.util.HashMap<>();
            if (request.getMahalName() != null) venueData.put("mahalName", request.getMahalName());
            if (request.getVenueName() != null) venueData.put("venueName", request.getVenueName());
            if (request.getVenueAddress() != null) venueData.put("venueAddress", request.getVenueAddress());
            if (request.getVenueCity() != null) venueData.put("venueCity", request.getVenueCity());
            if (request.getState() != null) venueData.put("state", request.getState());
            if (request.getMapLink() != null) venueData.put("mapLink", request.getMapLink());
            invite.setVenueData(venueData);

            // storyData (photos)
            Map<String, Object> storyData = invite.getStoryData() != null ? new java.util.HashMap<>(invite.getStoryData()) : new java.util.HashMap<>();
            if (request.getPhotos() != null) storyData.put("photos", request.getPhotos());
            invite.setStoryData(storyData);

            // scheduleData (events)
            Map<String, Object> scheduleData = invite.getScheduleData() != null ? new java.util.HashMap<>(invite.getScheduleData()) : new java.util.HashMap<>();
            if (request.getEventSchedule() != null) scheduleData.put("items", request.getEventSchedule());
            scheduleData.put("showSchedule", true);
            scheduleData.put("showGallery", true);
            invite.setScheduleData(scheduleData);
        } else {
            // Fallback: merge JSONB maps
            if (request.getCoupleData() != null) invite.setCoupleData(mergeMap(invite.getCoupleData(), request.getCoupleData()));
            if (request.getHeroData() != null) invite.setHeroData(mergeMap(invite.getHeroData(), request.getHeroData()));
            if (request.getVenueData() != null) invite.setVenueData(mergeMap(invite.getVenueData(), request.getVenueData()));
            if (request.getStoryData() != null) invite.setStoryData(mergeMap(invite.getStoryData(), request.getStoryData()));
            if (request.getScheduleData() != null) invite.setScheduleData(mergeMap(invite.getScheduleData(), request.getScheduleData()));
        }
    }

    private Map<String, Object> mergeMap(Map<String, Object> existing, Map<String, Object> updates) {
        Map<String, Object> result = existing != null ? new java.util.HashMap<>(existing) : new java.util.HashMap<>();
        result.putAll(updates);
        return result;
    }

    @Override
    @Transactional
    public Invite updateInvite(User user, UUID id, InviteRequest request) {
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invite not found with id: " + id));

        if (!invite.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this invite");
        }

        if (request.getTemplateId() != null) invite.setTemplateId(request.getTemplateId());
        if (request.getSlug() != null && !request.getSlug().isBlank() && invite.getSlug() == null) {
            invite.setSlug(request.getSlug());
        }

        if (request.getStatus() != null) {
            try {
                invite.setStatus(Invite.InviteStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        applyFlatFields(invite, request);

        if (request.getInvitationData() != null) invite.setInvitationData(mergeMap(invite.getInvitationData(), request.getInvitationData()));
        if (request.getEventData() != null) invite.setEventData(mergeMap(invite.getEventData(), request.getEventData()));
        if (request.getRsvpData() != null) invite.setRsvpData(mergeMap(invite.getRsvpData(), request.getRsvpData()));

        return inviteRepository.save(invite);
    }

    @Override
    @Transactional
    public void deleteInvite(User user, UUID id) {
        Invite invite = inviteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invite not found with id: " + id));

        if (!invite.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this invite");
        }

        inviteRepository.delete(invite);
    }

    @Override
    public Optional<Invite> getInviteByCode(String code) {
        // Try slug first (case-insensitive), then uppercase code
        Optional<Invite> bySlug = inviteRepository.findBySlugIgnoreCase(code);
        if (bySlug.isPresent()) return bySlug;
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
