package com.pokevault.modules.order;

import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.order.service.DiscountService;
import com.pokevault.modules.order.strategy.DiscountStrategy;
import com.pokevault.modules.order.strategy.RegularDiscountStrategy;
import com.pokevault.modules.order.strategy.VipDiscountStrategy;
import com.pokevault.modules.order.strategy.WholesaleDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountStrategyTest {

    private RegularDiscountStrategy regularDiscountStrategy;
    private VipDiscountStrategy vipDiscountStrategy;
    private WholesaleDiscountStrategy wholesaleDiscountStrategy;
    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        regularDiscountStrategy = new RegularDiscountStrategy();
        vipDiscountStrategy = new VipDiscountStrategy();
        wholesaleDiscountStrategy = new WholesaleDiscountStrategy();

        List<DiscountStrategy> strategies = List.of(
                regularDiscountStrategy,
                vipDiscountStrategy,
                wholesaleDiscountStrategy
        );
        discountService = new DiscountService(strategies);
    }

    @Test
    @DisplayName("RegularDiscountStrategy should support REGULAR tier and offer 0% discount")
    void testRegularDiscountStrategy() {
        assertThat(regularDiscountStrategy.supports(MembershipTier.REGULAR)).isTrue();
        assertThat(regularDiscountStrategy.supports(MembershipTier.VIP)).isFalse();
        assertThat(regularDiscountStrategy.supports(MembershipTier.WHOLESALE)).isFalse();

        assertThat(regularDiscountStrategy.getDiscountPercentage())
                .isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal discount = regularDiscountStrategy.calculate(subtotal);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("VipDiscountStrategy should support VIP tier and offer 10% discount")
    void testVipDiscountStrategy() {
        assertThat(vipDiscountStrategy.supports(MembershipTier.VIP)).isTrue();
        assertThat(vipDiscountStrategy.supports(MembershipTier.REGULAR)).isFalse();
        assertThat(vipDiscountStrategy.supports(MembershipTier.WHOLESALE)).isFalse();

        assertThat(vipDiscountStrategy.getDiscountPercentage())
                .isEqualByComparingTo(new BigDecimal("10.00"));

        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal discount = vipDiscountStrategy.calculate(subtotal);
        assertThat(discount).isEqualByComparingTo(new BigDecimal("100.00"));

        BigDecimal subtotal990 = new BigDecimal("990.00");
        BigDecimal discount99 = vipDiscountStrategy.calculate(subtotal990);
        assertThat(discount99).isEqualByComparingTo(new BigDecimal("99.00"));
    }

    @Test
    @DisplayName("WholesaleDiscountStrategy should support WHOLESALE tier and offer 15% discount")
    void testWholesaleDiscountStrategy() {
        assertThat(wholesaleDiscountStrategy.supports(MembershipTier.WHOLESALE)).isTrue();
        assertThat(wholesaleDiscountStrategy.supports(MembershipTier.REGULAR)).isFalse();
        assertThat(wholesaleDiscountStrategy.supports(MembershipTier.VIP)).isFalse();

        assertThat(wholesaleDiscountStrategy.getDiscountPercentage())
                .isEqualByComparingTo(new BigDecimal("15.00"));

        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal discount = wholesaleDiscountStrategy.calculate(subtotal);
        assertThat(discount).isEqualByComparingTo(new BigDecimal("150.00"));

        BigDecimal subtotal500 = new BigDecimal("500.00");
        BigDecimal discount75 = wholesaleDiscountStrategy.calculate(subtotal500);
        assertThat(discount75).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("DiscountService should calculate correct discount for each membership tier via Strategy Pattern")
    void testDiscountServiceCalculation() {
        BigDecimal subtotal = new BigDecimal("1000.00");

        BigDecimal regularDiscount = discountService.calculateDiscount(MembershipTier.REGULAR, subtotal);
        assertThat(regularDiscount).isEqualByComparingTo(new BigDecimal("0.00"));

        BigDecimal vipDiscount = discountService.calculateDiscount(MembershipTier.VIP, subtotal);
        assertThat(vipDiscount).isEqualByComparingTo(new BigDecimal("100.00"));

        BigDecimal wholesaleDiscount = discountService.calculateDiscount(MembershipTier.WHOLESALE, subtotal);
        assertThat(wholesaleDiscount).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("DiscountService should return zero discount for null, zero, or negative subtotal")
    void testDiscountServiceEdgeCases() {
        assertThat(discountService.calculateDiscount(MembershipTier.VIP, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(discountService.calculateDiscount(MembershipTier.VIP, BigDecimal.ZERO))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(discountService.calculateDiscount(MembershipTier.VIP, new BigDecimal("-100.00")))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("DiscountService should return applicable strategy according to membership tier")
    void testGetApplicableStrategy() {
        DiscountStrategy vipStrategy = discountService.getApplicableStrategy(MembershipTier.VIP);
        assertThat(vipStrategy).isInstanceOf(VipDiscountStrategy.class);

        DiscountStrategy regularStrategy = discountService.getApplicableStrategy(MembershipTier.REGULAR);
        assertThat(regularStrategy).isInstanceOf(RegularDiscountStrategy.class);

        DiscountStrategy wholesaleStrategy = discountService.getApplicableStrategy(MembershipTier.WHOLESALE);
        assertThat(wholesaleStrategy).isInstanceOf(WholesaleDiscountStrategy.class);
    }
}
