package com.pokevault.modules.order.strategy;

import com.pokevault.domain.enums.MembershipTier;

import java.math.BigDecimal;

public interface DiscountStrategy {

    BigDecimal calculate(BigDecimal subtotal);

    boolean supports(MembershipTier tier);

    BigDecimal getDiscountPercentage();
}
