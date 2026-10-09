package com.pokevault.modules.web;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.modules.web.service.StoreAdminServiceImpl;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ StoreAdminServiceImpl (ปรับราคาการ์ด / กำหนดระดับสมาชิก)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@ExtendWith(MockitoExtension.class)
class StoreAdminServiceTest {

    @Mock
    private CardInventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    private StoreAdminServiceImpl storeAdminService;

    @BeforeEach
    void setUp() {
        storeAdminService = new StoreAdminServiceImpl(inventoryRepository, userRepository);
    }

    @Test
    @DisplayName("updateSellingPrice: ราคาถูกต้อง ควรบันทึกราคาใหม่ทศนิยม 2 ตำแหน่ง")
    void updateSellingPrice_WhenValid_ShouldSaveNewPrice() {
        CardInventory inventory = CardInventory.builder().id(4L).quantity(4).sellingPrice(new BigDecimal("220.00")).build();
        when(inventoryRepository.findById(4L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(CardInventory.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = storeAdminService.updateSellingPrice(4L, new BigDecimal("259.5"));

        assertThat(inventory.getSellingPrice()).isEqualByComparingTo("259.50");
        assertThat(result.get("sellingPrice")).isEqualTo(new BigDecimal("259.50"));
    }

    @Test
    @DisplayName("updateSellingPrice: ราคาติดลบ ควรโยน IllegalArgumentException และไม่บันทึก")
    void updateSellingPrice_WhenNegative_ShouldReject() {
        assertThatThrownBy(() -> storeAdminService.updateSellingPrice(4L, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSellingPrice: ไม่พบ inventory ควรโยน ResourceNotFoundException")
    void updateSellingPrice_WhenMissing_ShouldThrowNotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeAdminService.updateSellingPrice(99L, BigDecimal.TEN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateMembershipTier: ลูกค้า ควรเปลี่ยนระดับสมาชิกในโปรไฟล์")
    void updateMembershipTier_WhenCustomer_ShouldChangeTier() {
        User customer = User.builder().id(3L).username("customer_red").role(UserRole.CUSTOMER).build();
        customer.setUserProfile(UserProfile.builder().user(customer).fullName("Red Champion").membershipTier(MembershipTier.REGULAR).build());
        when(userRepository.findById(3L)).thenReturn(Optional.of(customer));

        Map<String, Object> result = storeAdminService.updateMembershipTier(3L, MembershipTier.WHOLESALE);

        assertThat(customer.getUserProfile().getMembershipTier()).isEqualTo(MembershipTier.WHOLESALE);
        assertThat(result.get("membershipTier")).isEqualTo(MembershipTier.WHOLESALE);
        verify(userRepository).save(customer);
    }

    @Test
    @DisplayName("updateMembershipTier: ผู้ใช้ที่ไม่ใช่ลูกค้า ควรโยน IllegalArgumentException")
    void updateMembershipTier_WhenNotCustomer_ShouldReject() {
        User staff = User.builder().id(2L).username("staff_ash").role(UserRole.STAFF).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));

        assertThatThrownBy(() -> storeAdminService.updateMembershipTier(2L, MembershipTier.VIP))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).save(any());
    }
}
