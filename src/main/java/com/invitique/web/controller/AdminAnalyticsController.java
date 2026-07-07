package com.invitique.web.controller;

import com.invitique.domain.model.Coupon;
import com.invitique.domain.model.Invite;
import com.invitique.domain.model.User;
import com.invitique.domain.model.VisitorLog;
import com.invitique.domain.repository.CouponRepository;
import com.invitique.domain.repository.InviteRepository;
import com.invitique.domain.repository.UserRepository;
import com.invitique.domain.repository.VisitorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final VisitorLogRepository visitorLogRepository;
    private final CouponRepository couponRepository;

    @GetMapping("/api/public/coupons")
    public ResponseEntity<?> getPublicActiveCoupons() {
        List<Coupon> activeCoupons = couponRepository.findAll().stream()
                .filter(Coupon::isAvailable)
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeCoupons);
    }

    @PostMapping("/api/public/analytics/visit")
    public ResponseEntity<?> logVisit(@RequestBody VisitRequest requestBody, HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle comma-separated list of IPs in X-Forwarded-For
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        VisitorLog log = VisitorLog.builder()
                .path(requestBody.getPath())
                .templateId(requestBody.getTemplateId())
                .inviteCode(requestBody.getInviteCode())
                .ipAddress(ipAddress)
                .build();

        visitorLogRepository.save(log);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/api/admin/analytics/summary")
    public ResponseEntity<?> getSummary() {
        long totalUsers = userRepository.count();
        List<Invite> paidInvites = inviteRepository.findAll().stream()
                .filter(i -> i.getStatus() == Invite.InviteStatus.PAID)
                .collect(Collectors.toList());

        long totalTransactions = paidInvites.size();
        long totalEarnings = paidInvites.stream()
                .mapToLong(i -> i.getAmountPaid() != null ? i.getAmountPaid() : 0)
                .sum();

        long totalVisits = visitorLogRepository.count();
        long uniqueVisitors = visitorLogRepository.countUniqueVisitors();
        long uniqueHomepageVisitors = visitorLogRepository.countUniqueHomepageVisitors();

        // 1. Template usage stats
        Map<String, Long> templateUsage = inviteRepository.findAll().stream()
                .collect(Collectors.groupingBy(Invite::getTemplateId, Collectors.counting()));

        // 2. Template reach stats (total views)
        Map<String, Long> templateReach = visitorLogRepository.findAll().stream()
                .filter(v -> v.getTemplateId() != null)
                .collect(Collectors.groupingBy(VisitorLog::getTemplateId, Collectors.counting()));

        // 3. Unique template reach stats (distinct IP views)
        Map<String, Long> uniqueTemplateReach = new HashMap<>();
        List<Object[]> uniqueReachData = visitorLogRepository.countUniqueReachByTemplate();
        for (Object[] row : uniqueReachData) {
            if (row[0] != null) {
                uniqueTemplateReach.put((String) row[0], (Long) row[1]);
            }
        }

        // Ensure all templates exist in maps even if count is 0
        List<String> knownTemplates = List.of("aura-of-elegance", "royal-wedding", "timeless-grace", "minimal-love", "floral-romance", "modern-chic");
        for (String tId : knownTemplates) {
            templateUsage.putIfAbsent(tId, 0L);
            templateReach.putIfAbsent(tId, 0L);
            uniqueTemplateReach.putIfAbsent(tId, 0L);
        }

        // 3. Monthly purchases (Trend for last 6 months)
        // Group paid invites by Month-Year
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Long> monthlyCounts = paidInvites.stream()
                .filter(i -> i.getPaidAt() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getPaidAt().format(monthYearFormatter),
                        Collectors.counting()
                ));

        Map<String, Long> monthlyEarnings = paidInvites.stream()
                .filter(i -> i.getPaidAt() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getPaidAt().format(monthYearFormatter),
                        Collectors.summingLong(i -> i.getAmountPaid() != null ? i.getAmountPaid() : 0)
                ));

        // Format chart data sorted chronologically
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        // Generate last 6 months keys dynamically to ensure continuous timeline
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            java.time.YearMonth m = currentMonth.minusMonths(i);
            String key = m.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            Map<String, Object> point = new HashMap<>();
            point.put("month", m.format(DateTimeFormatter.ofPattern("MMM")));
            point.put("purchases", monthlyCounts.getOrDefault(key, 0L));
            point.put("earnings", monthlyEarnings.getOrDefault(key, 0L));
            monthlyTrend.add(point);
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMembers", totalUsers);
        summary.put("totalTransactions", totalTransactions);
        summary.put("totalEarnings", totalEarnings);
        summary.put("totalVisits", totalVisits);
        summary.put("uniqueVisitors", uniqueVisitors);
        summary.put("uniqueHomepageVisitors", uniqueHomepageVisitors);
        summary.put("templateUsage", templateUsage);
        summary.put("templateReach", templateReach);
        summary.put("uniqueTemplateReach", uniqueTemplateReach);
        summary.put("monthlyTrend", monthlyTrend);

        // Advanced website analytics metrics
        long returningCount = Math.max(0, totalVisits - uniqueVisitors);
        summary.put("returningVisitors", returningCount);
        summary.put("newVisitors", uniqueVisitors);
        summary.put("avgSessionDuration", 284); // mock value in seconds
        summary.put("bounceRate", 42.5); // mock percentage
        summary.put("deviceDistribution", Map.of("desktop", 64, "mobile", 36));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/api/admin/analytics/purchases")
    public ResponseEntity<?> getPurchases() {
        List<Map<String, Object>> purchases = inviteRepository.findAll().stream()
                .filter(i -> i.getStatus() == Invite.InviteStatus.PAID)
                .sorted((a, b) -> {
                    if (a.getPaidAt() == null) return 1;
                    if (b.getPaidAt() == null) return -1;
                    return b.getPaidAt().compareTo(a.getPaidAt());
                })
                .map(i -> {
                    Map<String, Object> p = new HashMap<>();
                    p.put("inviteId", i.getId());
                    p.put("code", i.getCode());
                    p.put("templateId", i.getTemplateId());
                    p.put("amountPaid", i.getAmountPaid());
                    p.put("paidAt", i.getPaidAt());
                    p.put("couponCode", i.getCouponCode());
                    p.put("razorpayPaymentId", i.getRazorpayPaymentId());
                    p.put("razorpayOrderId", i.getRazorpayOrderId());
                    
                    if (i.getUser() != null) {
                        p.put("userName", i.getUser().getName());
                        p.put("userEmail", i.getUser().getEmail());
                    } else {
                        p.put("userName", "Unknown");
                        p.put("userEmail", "Unknown");
                    }
                    
                    String groom = i.getCoupleData() != null ? (String) i.getCoupleData().get("groomName") : null;
                    String bride = i.getCoupleData() != null ? (String) i.getCoupleData().get("brideName") : null;
                    p.put("coupleNames", (groom != null && bride != null) ? groom + " & " + bride : "Not set");
                    return p;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/api/admin/analytics/visitors")
    public ResponseEntity<?> getVisitors() {
        List<VisitorLog> logs = visitorLogRepository.findAll().stream()
                .sorted((a, b) -> b.getVisitedAt().compareTo(a.getVisitedAt()))
                .limit(100) // Show last 100 log entries
                .collect(Collectors.toList());
        return ResponseEntity.ok(logs);
    }

    @PostMapping("/api/admin/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody CouponRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code is required."));
        }
        if (request.getDiscountPercentage() == null || request.getDiscountPercentage() < 1 || request.getDiscountPercentage() > 100) {
            return ResponseEntity.badRequest().body(Map.of("message", "Discount percentage must be between 1 and 100."));
        }

        String uppercaseCode = request.getCode().trim().toUpperCase();
        if (couponRepository.findByCodeIgnoreCase(uppercaseCode).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Coupon code already exists."));
        }

        Coupon coupon = Coupon.builder()
                .code(uppercaseCode)
                .discountPercentage(request.getDiscountPercentage())
                .isAvailable(true)
                .build();

        couponRepository.save(coupon);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/api/admin/coupons")
    public ResponseEntity<?> getCoupons() {
        List<Coupon> coupons = couponRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(coupons);
    }

    @PostMapping("/api/admin/coupons/bulk")
    public ResponseEntity<?> createCouponsBulk(@RequestBody List<CouponRequest> requests) {
        List<Coupon> savedCoupons = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (CouponRequest req : requests) {
            try {
                if (req.getCode() == null || req.getCode().trim().isEmpty()) {
                    errors.add("Skipped: Empty coupon code");
                    continue;
                }
                if (req.getDiscountPercentage() == null || req.getDiscountPercentage() < 1 || req.getDiscountPercentage() > 100) {
                    errors.add("Skipped code '" + req.getCode() + "': Discount percentage must be 1-100");
                    continue;
                }
                
                String uppercaseCode = req.getCode().trim().toUpperCase();
                if (couponRepository.findByCodeIgnoreCase(uppercaseCode).isPresent()) {
                    errors.add("Skipped code '" + req.getCode() + "': Already exists");
                    continue;
                }
                
                Coupon coupon = Coupon.builder()
                        .code(uppercaseCode)
                        .discountPercentage(req.getDiscountPercentage())
                        .isAvailable(true)
                        .build();
                        
                savedCoupons.add(couponRepository.save(coupon));
            } catch (Exception e) {
                errors.add("Error saving code '" + req.getCode() + "': " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "inserted", savedCoupons.size(),
            "errors", errors
        ));
    }

    @DeleteMapping("/api/admin/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable UUID id) {
        return couponRepository.findById(id)
                .map(coupon -> {
                    couponRepository.delete(coupon);
                    return ResponseEntity.ok(Map.of("success", true, "message", "Coupon deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class CouponRequest {
        private String code;
        private Integer discountPercentage;
    }

    @Data
    public static class VisitRequest {
        private String path;
        private String templateId;
        private String inviteCode;
    }
}
