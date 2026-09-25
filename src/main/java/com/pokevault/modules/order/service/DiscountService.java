package com.pokevault.modules.order.service;

import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.order.strategy.DiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DiscountService {

    private final List<DiscountStrategy> strategies;

    public DiscountService(List<DiscountStrategy> strategies) {
        this.strategies = strategies != null ? strategies : List.of();
    }

    public BigDecimal calculateDiscount(MembershipTier tier, BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        DiscountStrategy strategy = getApplicableStrategy(tier);
        return strategy.calculate(subtotal);
    }

    public DiscountStrategy getApplicableStrategy(MembershipTier tier) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(tier))
                .findFirst()
                .orElseGet(() -> strategies.stream()
                        .filter(strategy -> strategy.supports(MembershipTier.REGULAR))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No suitable discount strategy found for tier: " + tier)));
    }
}
