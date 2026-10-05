package com.pokevault.modules.order.strategy;

import com.pokevault.domain.enums.MembershipTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WholesaleDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.15");
    private static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("15.00");

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean supports(MembershipTier tier) {
        return MembershipTier.WHOLESALE == tier;
    }

    @Override
    public BigDecimal getDiscountPercentage() {
        return DISCOUNT_PERCENTAGE;
    }
}
