package com.invitique.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationRequest {

    @NotBlank(message = "Coupon code cannot be empty")
    private String code;
}
