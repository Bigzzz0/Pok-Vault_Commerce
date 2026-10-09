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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DiscountStrategyEdgeCaseTest: Member 3 GoF Strategy Pattern Edge Cases")
class DiscountStrategyEdgeCaseTest {

    private RegularDiscountStrategy regularStrategy;
    private VipDiscountStrategy vipStrategy;
    private WholesaleDiscountStrategy wholesaleStrategy;
    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        regularStrategy = new RegularDiscountStrategy();
        vipStrategy = new VipDiscountStrategy();
        wholesaleStrategy = new WholesaleDiscountStrategy();

        discountService = new DiscountService(List.of(
                regularStrategy,
                vipStrategy,
                wholesaleStrategy
        ));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-1", "-150.50"})
    @DisplayName("DiscountService: ส่ง subtotal เป็น null, ศูนย์ หรือติดลบ คืนค่าส่วนลด 0.00 เสมอ")
    void calculateDiscount_NullZeroOrNegativeSubtotal_ReturnsZero(String subtotalStr) {
        BigDecimal subtotal = subtotalStr != null ? new BigDecimal(subtotalStr) : null;

        BigDecimal discountVip = discountService.calculateDiscount(MembershipTier.VIP, subtotal);
        BigDecimal discountWholesale = discountService.calculateDiscount(MembershipTier.WHOLESALE, subtotal);

        assertThat(discountVip).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(discountVip.scale()).isEqualTo(2);
        assertThat(discountWholesale).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(discountWholesale.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("DiscountService: เมื่อ tier เป็น null ให้ Fallback ไปใช้ Regular Strategy (ส่วนลด 0%)")
    void calculateDiscount_NullTier_FallsBackToRegularStrategy() {
        BigDecimal subtotal = new BigDecimal("1000.00");

        BigDecimal discount = discountService.calculateDiscount(null, subtotal);

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(discountService.getApplicableStrategy(null)).isInstanceOf(RegularDiscountStrategy.class);
    }

    @Test
    @DisplayName("VipDiscountStrategy: ตรวจสอบการปัดเศษ HALF_UP ทศนิยม 2 ตำแหน่งอย่างแม่นยำ")
    void vipStrategy_RoundingPrecisionHalfUp() {
        // 10% ของ 99.99 = 9.999 -> ปัดขึ้นเป็น 10.00
        BigDecimal subtotal1 = new BigDecimal("99.99");
        assertThat(vipStrategy.calculate(subtotal1)).isEqualByComparingTo(new BigDecimal("10.00"));

        // 10% ของ 99.94 = 9.994 -> ปัดลงเป็น 9.99
        BigDecimal subtotal2 = new BigDecimal("99.94");
        assertThat(vipStrategy.calculate(subtotal2)).isEqualByComparingTo(new BigDecimal("9.99"));

        // 10% ของ 99.95 = 9.995 -> ปัดขึ้นเป็น 10.00
        BigDecimal subtotal3 = new BigDecimal("99.95");
        assertThat(vipStrategy.calculate(subtotal3)).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("WholesaleDiscountStrategy: ตรวจสอบการคำนวณส่วนลด 15% และการปัดเศษ HALF_UP")
    void wholesaleStrategy_RoundingPrecisionHalfUp() {
        // 15% ของ 33.33 = 4.9995 -> ปัดขึ้นเป็น 5.00
        BigDecimal subtotal1 = new BigDecimal("33.33");
        assertThat(wholesaleStrategy.calculate(subtotal1)).isEqualByComparingTo(new BigDecimal("5.00"));

        // 15% ของ 100.00 = 15.00
        BigDecimal subtotal2 = new BigDecimal("100.00");
        assertThat(wholesaleStrategy.calculate(subtotal2)).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("DiscountService: รองรับตัวเลขยอดขายขนาดใหญ่มาก (Large Scale) โดยไม่สูญเสียความแม่นยำ")
    void calculateDiscount_VeryLargeSubtotal_MaintainsPrecision() {
        BigDecimal largeSubtotal = new BigDecimal("100000000.00"); // 100 ล้านบาท
        BigDecimal wholesaleDiscount = discountService.calculateDiscount(MembershipTier.WHOLESALE, largeSubtotal);

        assertThat(wholesaleDiscount).isEqualByComparingTo(new BigDecimal("15000000.00"));
    }

    @Test
    @DisplayName("DiscountService: เมื่อไม่มี Strategy ในระบบ หรือไม่มี Regular Strategy เป็น Fallback โยน IllegalStateException")
    void discountService_EmptyStrategiesList_ThrowsIllegalStateException() {
        DiscountService emptyContext = new DiscountService(List.of());

        assertThatThrownBy(() -> emptyContext.calculateDiscount(MembershipTier.VIP, new BigDecimal("500.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No suitable discount strategy found");
    }

    @Test
    @DisplayName("DiscountService: Constructor ป้องกัน NullPointerException เมื่อส่ง null strategies list")
    void discountService_NullConstructorList_DefaultsToEmptyListAndThrowsOnLookup() {
        DiscountService nullContext = new DiscountService(null);

        assertThatThrownBy(() -> nullContext.getApplicableStrategy(MembershipTier.VIP))
                .isInstanceOf(IllegalStateException.class);
    }
}
