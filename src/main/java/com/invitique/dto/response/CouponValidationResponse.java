package com.invitique.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {
    @com.fasterxml.jackson.annotation.JsonProperty("isValid")
    private boolean isValid;
    private String code;
    private Integer discountPercentage;
    private String message;
}
