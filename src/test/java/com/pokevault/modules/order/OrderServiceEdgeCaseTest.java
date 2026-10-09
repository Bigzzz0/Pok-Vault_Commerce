package com.pokevault.modules.order;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.modules.order.service.DiscountService;
import com.pokevault.modules.order.service.OrderServiceImpl;
import com.pokevault.modules.order.strategy.RegularDiscountStrategy;
import com.pokevault.modules.order.strategy.VipDiscountStrategy;
import com.pokevault.modules.order.strategy.WholesaleDiscountStrategy;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceEdgeCaseTest: Member 3 Order Engine Business Logic Edge Cases")
class OrderServiceEdgeCaseTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CardInventoryRepository cardInventoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private DiscountService discountService;
    private OrderServiceImpl orderService;

    private User sampleUser;
    private Card cardA;
    private Card cardB;
    private CardInventory invA;
    private CardInventory invB;

    @BeforeEach
    void setUp() {
        // ใช้ DiscountService ตัวจริงพร้อม Strategy ครบทุกตัว เพื่อทดสอบการคำนวณส่วนลดจริง (ไม่ใช้ Mock)
        discountService = new DiscountService(List.of(
                new RegularDiscountStrategy(),
                new VipDiscountStrategy(),
                new WholesaleDiscountStrategy()
        ));

        orderService = new OrderServiceImpl(
                orderRepository,
                cardInventoryRepository,
                userRepository,
                discountService,
                eventPublisher
        );

        sampleUser = User.builder().id(1L).username("trainer_red").email("red@kanto.test").build();

        cardA = Card.builder().id(101L).name("Pikachu ex").cardNumber("001/100").build();
        invA = CardInventory.builder().id(10L).card(cardA).condition(CardCondition.MINT)
                .quantity(10).sellingPrice(new BigDecimal("500.00")).build();

        cardB = Card.builder().id(102L).name("Mewtwo ex").cardNumber("002/100").build();
        invB = CardInventory.builder().id(20L).card(cardB).condition(CardCondition.NEAR_MINT)
                .quantity(3).sellingPrice(new BigDecimal("1500.00")).build();
    }

    @Test
    @DisplayName("Order Engine: สั่งซื้อหลายรายการ (Multi-item) หักสต็อกการ์ดแต่ละใบและคำนวณส่วนลด VIP 10% จริงถูกต้อง")
    void createOrder_MultiItem_DeductsStockAccurately() {
        UserProfile profile = UserProfile.builder().user(sampleUser).membershipTier(MembershipTier.VIP).build();
        sampleUser.setUserProfile(profile);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(cardInventoryRepository.findById(20L)).thenReturn(Optional.of(invB));
        when(orderRepository.count()).thenReturn(5L);

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

        // Subtotal = (500 * 2) + (1500 * 1) = 2500.00
        // VIP 10% คำนวณจริง = 250.00
        // Final = 2250.00
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
    @DisplayName("Order Engine: เมื่อการ์ดชิ้นที่ 2 ขาดสต็อก ต้องโยน InsufficientStockException และไม่บันทึก Order หรือส่ง Event")
    void createOrder_MultiItemInsufficientStockOnSecondItem_ThrowsExceptionAndNeverSavesOrder() {
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

        // หมายเหตุ: ในระดับ Mockito Unit Test ชิ้นแรก invA ถูกหักสต็อกไปก่อน (เหลือ 9)
        // การ Rollback แบบสมบูรณ์จะทำงานเมื่อรันบน Spring Transactional Container จริง
        assertThat(invA.getQuantity()).isEqualTo(9);
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Order Engine: โยน ResourceNotFoundException เมื่อไม่พบ userId และต้องไม่บันทึกออเดอร์")
    void createOrder_UserNotFound_ThrowsExceptionAndNeverSaves() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(999L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(cardInventoryRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Order Engine: โยน ResourceNotFoundException เมื่อไม่พบ inventoryId และต้องไม่บันทึกออเดอร์")
    void createOrder_InventoryNotFound_ThrowsExceptionAndNeverSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(999L)).thenReturn(Optional.empty());

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(999L).quantity(1).build()))
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CardInventory");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Order Engine: ลูกค้าที่ไม่มี UserProfile ให้ Fallback เป็นระดับ REGULAR อัตโนมัติ (ส่วนลด 0%)")
    void createOrder_UserWithoutProfile_DefaultsToRegularTier() {
        sampleUser.setUserProfile(null); // ไม่มีโปรไฟล์
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Order Engine: ลูกค้าระดับ WHOLESALE ได้รับส่วนลด 15% คำนวณจริงตาม WholesaleDiscountStrategy")
    void createOrder_WholesaleTier_Applies15PercentDiscount() {
        UserProfile profile = UserProfile.builder().user(sampleUser).membershipTier(MembershipTier.WHOLESALE).build();
        sampleUser.setUserProfile(profile);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlaceOrderRequest request = PlaceOrderRequest.builder()
                .userId(1L)
                .customerFriendId("1234-5678-9012-3456")
                .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(10L).quantity(1).build()))
                .build();

        OrderResponse response = orderService.createOrder(request);

        // Subtotal = 500.00 -> Wholesale 15% จริง = 75.00 -> Final = 425.00
        assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("425.00"));
    }

    @Test
    @DisplayName("Order Engine: การจัดรูปแบบ Order Code ตามลำดับจำนวนออเดอร์ ORD-YYYY-XXX")
    void createOrder_OrderCodeFormatting_GeneratesSequentialCodes() {
        int year = LocalDate.now().getYear();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cardInventoryRepository.findById(10L)).thenReturn(Optional.of(invA));
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
