package com.invitique.service;

import com.invitique.dto.response.CouponValidationResponse;

public interface CouponService {
    CouponValidationResponse validateCoupon(String code);
}
