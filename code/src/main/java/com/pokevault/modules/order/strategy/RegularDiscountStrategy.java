package com.pokevault.modules.order.strategy;

import com.pokevault.domain.enums.MembershipTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RegularDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal DISCOUNT_PERCENTAGE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculate(BigDecimal subtotal) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean supports(MembershipTier tier) {
        return MembershipTier.REGULAR == tier;
    }

    @Override
    public BigDecimal getDiscountPercentage() {
        return DISCOUNT_PERCENTAGE;
    }
}
