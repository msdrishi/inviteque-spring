package com.invitique.web.controller;

import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.service.InviteService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
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

            int amountInRupees = 999; // Default price
            int finalPrice = amountInRupees;
            if (request.getDiscountPercentage() != null) {
                int discount = (amountInRupees * request.getDiscountPercentage()) / 100;
                finalPrice = amountInRupees - discount;
            }
            
            int amountInPaise = finalPrice * 100;

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

            if (enabled) {
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
            if (request.getInviteRequest() != null) {
                // Force status to PAID
                request.getInviteRequest().setStatus("PAID");
                Invite updatedInvite = inviteService.createOrUpdateInvite(user, request.getInviteRequest());
                
                // Update payment fields in database
                updatedInvite.setRazorpayOrderId(request.getRazorpayOrderId());
                updatedInvite.setRazorpayPaymentId(request.getRazorpayPaymentId());
                updatedInvite.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : 999);
                updatedInvite.setPaidAt(LocalDateTime.now());
                inviteRepository.save(updatedInvite);
            } else {
                invite.setStatus(Invite.InviteStatus.PAID);
                invite.setRazorpayOrderId(request.getRazorpayOrderId());
                invite.setRazorpayPaymentId(request.getRazorpayPaymentId());
                invite.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : 999);
                invite.setPaidAt(LocalDateTime.now());
                inviteRepository.save(invite);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Payment verified and saved successfully"));

        } catch (Exception e) {
            log.error("Failed to verify payment", e);
            return ResponseEntity.internalServerError().body(Map.of("message", "Payment verification error: " + e.getMessage()));
        }
    }

    @Data
    public static class OrderRequest {
        private String code;
        private Integer discountPercentage;
    }

    @Data
    public static class PaymentVerificationRequest {
        private String inviteCode;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
        private Integer amountPaid;
        private com.invitique.dto.request.InviteRequest inviteRequest;
    }
}
