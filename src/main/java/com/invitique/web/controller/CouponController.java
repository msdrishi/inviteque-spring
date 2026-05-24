package com.invitique.web.controller;

import com.invitique.dto.request.CouponValidationRequest;
import com.invitique.dto.response.CouponValidationResponse;
import com.invitique.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @Valid @RequestBody CouponValidationRequest request) {
        CouponValidationResponse response = couponService.validateCoupon(request.getCode());
        return ResponseEntity.ok(response);
    }
}
