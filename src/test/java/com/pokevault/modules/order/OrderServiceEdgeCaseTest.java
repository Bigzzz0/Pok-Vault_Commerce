package com.pokevault.modules.order;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.modules.order.service.DiscountService;
import com.pokevault.modules.order.service.OrderServiceImpl;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceEdgeCaseTest: Member 3 Order Engine Business Logic Edge Cases")
class OrderServiceEdgeCaseTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CardInventoryRepository cardInventoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private DiscountService discountService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User sampleUser;
    private Card cardA;
    private Card cardB;
    private CardInventory invA;
    private CardInventory invB;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).username("trainer_red").email("red@kanto.test").build();

        cardA = Card.builder().id(101L).name("Pikachu ex").cardNumber("001/100").build();
        invA = CardInventory.builder().id(10L).card(cardA).condition(CardCondition.MINT)
                .quantity(10).sellingPrice(new BigDecimal("500.00")).build();

        cardB = Card.builder().id(102L).name("Mewtwo ex").cardNumber("002/100").build();
        invB = CardInventory.builder().id(20L).card(cardB).condition(CardCondition.NEAR_MINT)
                .quantity(3).sellingPrice(new BigDecimal("1500.00")).build();
    }

    @Test
    @DisplayName("Order Engine: สั่งซื้อหลายรายการ (Multi-item) หักสต็อกการ์ดแต่ละใบและคำนวณราคารวมถูกต้อง")
    void createOrder_MultiItem_DeductsStockAccurately() {
        UserProfile profile = UserProfile.builder().user(sampleUser).membershipTier(MembershipTier.VIP).build();
        sampleUser.setUserProfile(profile);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(cardInventoryRepository.findById(20L)).thenReturn(Optional.of(invB));
        when(orderRepository.count()).thenReturn(5L);

        // Subtotal = (500 * 2) + (1500 * 1) = 2500.00
        // VIP 10% discount = 250.00
        when(discountService.calculateDiscount(eq(MembershipTier.VIP), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("250.00"));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .customerInGameName("RedMaster")
                .items(List.of(
                        PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(2).build(),
                        PlaceOrderRequest.OrderItemRequest.builder().inventoryId(20L).quantity(1).build()
                ))
                .notes("Handle with care")
                .build();

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("2250.00"));
        assertThat(response.getItems()).hasSize(2);

        // Verify stock deducted
        assertThat(invA.getQuantity()).isEqualTo(8);
        assertThat(invB.getQuantity()).isEqualTo(2);
        verify(cardInventoryRepository, times(1)).save(invA);
        verify(cardInventoryRepository, times(1)).save(invB);

        // Verify OrderPlacedEvent captures both items
        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        OrderPlacedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(100L);
        assertThat(publishedEvent.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Order Engine: หากสินค้าชิ้นที่ 2 สต็อกไม่พอ ต้องโยน InsufficientStockException และไม่อนุญาตให้เซฟออเดอร์")
    void createOrder_MultiItemInsufficientStockOnSecondItem_AbortsOrder() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        invB.setQuantity(0); // ชิ้นที่ 2 สต็อกหมด
        when(cardInventoryRepository.findById(20L)).thenReturn(Optional.of(invB));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(
                        PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build(),
                        PlaceOrderRequest.OrderItemRequest.builder().inventoryId(20L).quantity(1).build()
                ))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Mewtwo ex");

        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Order Engine: ลูกค้าที่ไม่มี UserProfile ให้ Fallback เป็นระดับ REGULAR อัตโนมัติ (ส่วนลด 0%)")
    void createOrder_UserWithoutProfile_DefaultsToRegularTier() {
        sampleUser.setUserProfile(null); // ไม่มีโปรไฟล์
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(discountService.calculateDiscount(eq(MembershipTier.REGULAR), any(BigDecimal.class)))
                .thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(discountService).calculateDiscount(eq(MembershipTier.REGULAR), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Order Engine: ลูกค้าระดับ WHOLESALE ได้รับส่วนลด 15% ตาม Strategy Pattern")
    void createOrder_WholesaleTier_Applies15PercentDiscount() {
        UserProfile profile = UserProfile.builder().user(sampleUser).membershipTier(MembershipTier.WHOLESALE).build();
        sampleUser.setUserProfile(profile);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(discountService.calculateDiscount(eq(MembershipTier.WHOLESALE), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("75.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("425.00"));
        verify(discountService).calculateDiscount(eq(MembershipTier.WHOLESALE), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Order Engine: การจัดรูปแบบ Order Code ตามลำดับจำนวนออเดอร์ ORD-YYYY-XXX")
    void createOrder_OrderCodeFormatting_GeneratesSequentialCodes() {
        int year = LocalDate.now().getYear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        // กรณีออเดอร์ลำดับที่ 10 (count = 9) -> ORD-YYYY-010
        when(orderRepository.count()).thenReturn(9L);
        OrderResponse resp10 = orderService.createOrder(request);
        assertThat(resp10.getOrderCode()).isEqualTo(String.format("ORD-%d-010", year));

        // กรณีออเดอร์ลำดับที่ 100 (count = 99) -> ORD-YYYY-100
        when(orderRepository.count()).thenReturn(99L);
        OrderResponse resp100 = orderService.createOrder(request);
        assertThat(resp100.getOrderCode()).isEqualTo(String.format("ORD-%d-100", year));
    }

    @Test
    @DisplayName("Order Engine: เก็บและส่งคืน Notes และ Customer In-Game Name ใน Order Entity และ Response")
    void createOrder_MapsCustomerNotesAndInGameName() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .customerInGameName("SatoshiTrainer")
                .notes("Please package with bubble wrap")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getCustomerInGameName()).isEqualTo("SatoshiTrainer");
        assertThat(response.getNotes()).isEqualTo("Please package with bubble wrap");
    }
}
