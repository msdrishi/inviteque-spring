package com.invitique.service.impl;

import com.invitique.domain.model.Coupon;
import com.invitique.domain.repository.CouponRepository;
import com.invitique.dto.response.CouponValidationResponse;
import com.invitique.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code) {
        if (code == null || code.trim().isEmpty()) {
            return CouponValidationResponse.builder()
                    .isValid(false)
                    .code("")
                    .discountPercentage(0)
                    .message("Coupon code cannot be empty.")
                    .build();
        }

        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCase(code.trim());

        if (couponOpt.isEmpty()) {
            return CouponValidationResponse.builder()
                    .isValid(false)
                    .code(code)
                    .discountPercentage(0)
                    .message("Invalid coupon code.")
                    .build();
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.isAvailable()) {
            return CouponValidationResponse.builder()
                    .isValid(false)
                    .code(coupon.getCode())
                    .discountPercentage(0)
                    .message("This coupon has already been used or is unavailable.")
                    .build();
        }

        return CouponValidationResponse.builder()
                    .isValid(true)
                    .code(coupon.getCode())
                    .discountPercentage(coupon.getDiscountPercentage())
                    .message("Coupon applied! " + coupon.getDiscountPercentage() + "% discount applied successfully.")
                    .build();
    }
}
