package com.invitique.web.controller;

import com.invitique.domain.model.Coupon;
import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.CouponRepository;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.service.InviteService;
import com.invitique.service.EmailService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    @Value("${razorpay.key-id:}")
    private String keyId;

    @Value("${razorpay.key-secret:}")
    private String keySecret;

    @Value("${razorpay.enabled:false}")
    private boolean enabled;

    private final InviteRepository inviteRepository;
    private final InviteService inviteService;
    private final CouponRepository couponRepository;
    private final EmailService emailService;
    private final com.invitique.domain.repository.TemplateRepository templateRepository;
    private final com.invitique.service.RsvpService rsvpService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody OrderRequest request) {
        
        log.info("Payment order request received for invite code: {}, enabled: {}", request.getCode(), enabled);
        
        if (!enabled) {
            return ResponseEntity.ok(Map.of("enabled", false));
        }

        try {
            Invite invite = inviteRepository.findByCode(request.getCode())
                    .orElseThrow(() -> new RuntimeException("Invite not found with code: " + request.getCode()));

            // Safety check: ensure invite belongs to user
            if (!invite.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
            }

            boolean isTemplatePaid = invite.getStatus() == Invite.InviteStatus.PAID;
            boolean alreadyHasRsvp = Boolean.TRUE.equals(invite.getHasRsvp());
            boolean wantsRsvp = Boolean.TRUE.equals(request.getWantsRsvp());

            // Query dynamic base template price from master templates table with catalog fallback
            double baseTemplatePrice = resolveBasePrice(invite.getTemplateId());

            double templateCharge = isTemplatePaid ? 0.0 : baseTemplatePrice;
            double rsvpCharge = (isTemplatePaid ? (!alreadyHasRsvp && wantsRsvp) : wantsRsvp) ? 500.0 : 0.0;
            double calculatedBaseTotal = templateCharge + rsvpCharge;

            // Handle coupon validation securely on backend
            double discount = 0.0;
            String couponCode = request.getCouponCode();
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(couponCode.trim());
                if (couponOpt.isPresent() && couponOpt.get().isAvailable()) {
                    discount = (calculatedBaseTotal * couponOpt.get().getDiscountPercentage()) / 100.0;
                }
            } else if (request.getDiscountPercentage() != null && request.getDiscountPercentage() > 0) {
                discount = (calculatedBaseTotal * request.getDiscountPercentage()) / 100.0;
            }

            double finalPrice = Math.max(0.0, calculatedBaseTotal - discount);
            // If already fully paid and nothing to charge
            if (isTemplatePaid && (alreadyHasRsvp || !wantsRsvp)) {
                finalPrice = 0.0;
            }
            
            int amountInPaise = (int) Math.round(finalPrice * 100.0);

            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", invite.getCode());

            Order order = razorpay.orders.create(orderRequest);
            String razorpayOrderId = order.get("id");

            invite.setRazorpayOrderId(razorpayOrderId);
            inviteRepository.save(invite);

            return ResponseEntity.ok(Map.of(
                    "enabled", true,
                    "orderId", razorpayOrderId,
                    "amount", amountInPaise,
                    "keyId", keyId,
                    "currency", "INR"
            ));

        } catch (Exception e) {
            log.error("Failed to create Razorpay order", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to initialize payment: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @AuthenticationPrincipal User user,
            @RequestBody PaymentVerificationRequest request) {
        
        log.info("Payment verification request received for order: {}, payment: {}, enabled: {}", 
                request.getRazorpayOrderId(), request.getRazorpayPaymentId(), enabled);

        try {
            Invite invite = inviteRepository.findByCode(request.getInviteCode())
                    .orElseThrow(() -> new RuntimeException("Invite not found with code: " + request.getInviteCode()));

            if (!invite.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
            }

            String couponCode = request.getCouponCode();
            if ((couponCode == null || couponCode.trim().isEmpty()) && request.getInviteRequest() != null) {
                couponCode = request.getInviteRequest().getCouponCode();
            }

            boolean isFree = false;
            if (request.getAmountPaid() != null && Math.abs(request.getAmountPaid() - 0.0) < 1e-4) {
                isFree = true;
            }

            if (enabled && !isFree) {
                // Verify Razorpay signature
                JSONObject options = new JSONObject();
                options.put("razorpay_order_id", request.getRazorpayOrderId());
                options.put("razorpay_payment_id", request.getRazorpayPaymentId());
                options.put("razorpay_signature", request.getRazorpaySignature());

                boolean isValid = Utils.verifyPaymentSignature(options, keySecret);
                if (!isValid) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Invalid payment signature verification failed"));
                }
            }

            // Save the invitation details passed in the request (or update status)
            Invite finalInvite;
            if (request.getInviteRequest() != null) {
                // Force status to PAID and link to the existing invite
                request.getInviteRequest().setStatus("PAID");
                if (request.getInviteRequest().getId() == null && invite.getId() != null) {
                    request.getInviteRequest().setId(invite.getId().toString());
                }
                if (request.getInviteRequest().getCode() == null && invite.getCode() != null) {
                    request.getInviteRequest().setCode(invite.getCode());
                }
                finalInvite = inviteService.createOrUpdateInvite(user, request.getInviteRequest());
            } else {
                invite.setStatus(Invite.InviteStatus.PAID);
                finalInvite = invite;
            }
            
            boolean isTemplatePaid = invite.getStatus() == Invite.InviteStatus.PAID;
            boolean alreadyHasRsvp = Boolean.TRUE.equals(invite.getHasRsvp());
            boolean wantsRsvp = request.getInviteRequest() != null
                    ? (Boolean.TRUE.equals(request.getInviteRequest().getHasRsvp()) ||
                       (request.getInviteRequest().getRsvpData() != null && Boolean.TRUE.equals(request.getInviteRequest().getRsvpData().get("enabled"))))
                    : false;

            boolean grantRsvp = alreadyHasRsvp || (wantsRsvp && ((request.getAmountPaid() != null && request.getAmountPaid() >= 500.0) || isFree || !enabled));

            finalInvite.setHasRsvp(grantRsvp);

            Map<String, Object> rData = finalInvite.getRsvpData() != null ? new java.util.HashMap<>(finalInvite.getRsvpData()) : new java.util.HashMap<>();
            rData.put("hasRsvp", grantRsvp);
            rData.put("isPaid", grantRsvp);
            rData.put("enabled", grantRsvp);
            finalInvite.setRsvpData(rData);

            Map<String, Object> iData = finalInvite.getInvitationData() != null ? new java.util.HashMap<>(finalInvite.getInvitationData()) : new java.util.HashMap<>();
            iData.put("hasRsvp", grantRsvp);
            iData.put("isRsvpPaid", grantRsvp);
            finalInvite.setInvitationData(iData);

            finalInvite.setRazorpayOrderId(request.getRazorpayOrderId());
            finalInvite.setRazorpayPaymentId(request.getRazorpayPaymentId());
            double totalPaid = (finalInvite.getAmountPaid() != null ? finalInvite.getAmountPaid() : 0.0) + (request.getAmountPaid() != null ? request.getAmountPaid() : 0.0);
            finalInvite.setAmountPaid(totalPaid > 0 ? totalPaid : (request.getAmountPaid() != null ? request.getAmountPaid() : 2499.0));
            finalInvite.setPaidAt(LocalDateTime.now());
            
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                finalInvite.setCouponCode(couponCode.trim());
                
                // Set the coupon as used / unavailable
                final String finalCouponCode = couponCode;
                couponRepository.findByCodeIgnoreCase(couponCode.trim()).ifPresent(coupon -> {
                    coupon.setAvailable(false);
                    coupon.setPurchasedDate(LocalDateTime.now());
                    coupon.setInviteId(finalInvite.getId());
                    couponRepository.save(coupon);
                });
            }
            inviteRepository.save(finalInvite);

            try {
                rsvpService.syncEventsFromScheduleData(finalInvite);
            } catch (Exception syncEx) {
                log.warn("Failed to sync RSVP events upon payment verification: ", syncEx);
            }

            try {
                emailService.sendOrderNotification(finalInvite, user);
            } catch (Exception mailEx) {
                log.error("Email notification sending failed: ", mailEx);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified and saved successfully"));

        } catch (Exception e) {
            log.error("Failed to verify payment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Payment verification error: " + e.getMessage()));
        }
    }

    private double resolveBasePrice(String templateId) {
        if (templateId == null || templateId.trim().isEmpty()) {
            return 2499.0;
        }
        String tid = templateId.trim().toLowerCase();

        // 1. Check database first - primary persistent source of truth
        Optional<com.invitique.domain.model.Template> tmplOpt = templateRepository.findByIdIgnoreCase(tid);
        if (tmplOpt.isPresent() && tmplOpt.get().getPrice() != null && tmplOpt.get().getPrice() > 0) {
            return tmplOpt.get().getPrice() / 100.0;
        }

        // Canonical catalog price mapping
        switch (tid) {
            case "southern-serenade":
            case "rajasthani-vows":
            case "rajestani-vows":
            case "royal-heritage":
            case "pink-blossom":
            case "midnight-waltz":
            case "royal-heirloom":
                return 2499.0;

            case "aura-of-elegance":
            case "template-1":
            case "twilight-serenade":
            case "template-2":
            case "sunflower-fields":
            case "sunflowerfields":
            case "template-3":
            case "blossom-whisper":
            case "everlastingvows":
            case "everlasting-vows":
            case "template-4":
            case "modernhearth":
            case "modern-hearth":
            case "enchanted-forest":
            case "template-8":
            case "modern-muse":
            case "template-10":
            case "earthy-whispers":
            case "template-11":
            case "coastal-serenity":
            case "template-12":
            case "template-5":
            case "celestial-union":
            case "template-6":
            case "infinite-journey":
            case "template-7":
            case "template-9":
            case "house-warming-1":
            case "house-warming-2":
            case "house-warming-3":
            case "royal-palace":
                return 1999.0;

            default:
                return 2499.0;
        }
    }

    @Data
    public static class OrderRequest {
        private String code;
        private Integer discountPercentage;
        private Double amount;
        private Boolean wantsRsvp;
        private String couponCode;
    }

    @Data
    public static class PaymentVerificationRequest {
        private String inviteCode;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private Double amountPaid;
        private String couponCode;
        private com.invitique.dto.request.InviteRequest inviteRequest;
    }
}
