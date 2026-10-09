package com.pokevault.modules.order;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.service.DiscountService;
import com.pokevault.modules.order.service.OrderServiceImpl;
import com.pokevault.modules.order.strategy.RegularDiscountStrategy;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.modules.trade.service.TradeMatchingServiceImpl;
import com.pokevault.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ชุดทดสอบครอบคลุมงานหลักของ สมาชิกคนที่ 3 (ธนภูมิ):
 * 1. จองใบสุดท้ายแล้วยังจับคู่ได้ (Remaining 0 stock can still match)
 * 2. จองหลายรายการแล้วรายการหนึ่งล้มเหลว ต้อง abort และไม่บันทึกออเดอร์
 * 3. เปลี่ยนบัญชีแล้วจำนวนสต็อกของทั้งสองคลังถูกต้อง (Transfer reservation)
 * 4. ยกเลิกคืนสต็อกครั้งเดียว ไปยัง Inventory ที่ถือการจองล่าสุด
 * 5. ตรวจสอบสิทธิ์เจ้าของออเดอร์ (Customer IDOR protection & Staff booking delegation)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderStockAndSecurityTest: Member 3 Stock Consistency, Account Reassignment & Ownership Security")
class OrderStockAndSecurityTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private CardInventoryRepository cardInventoryRepository;
    @Mock private GameAccountRepository gameAccountRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private OrderServiceImpl orderService;
    private TradeMatchingServiceImpl tradeMatchingService;

    private User customer1;
    private User customer2;
    private User staffUser;

    private Card charizard;
    private GameAccount accountA;
    private GameAccount accountB;
    private CardInventory inventoryA;
    private CardInventory inventoryB;

    @BeforeEach
    void setUp() {
        DiscountService discountService = new DiscountService(List.of(new RegularDiscountStrategy()));

        orderService = new OrderServiceImpl(
                orderRepository,
                cardInventoryRepository,
                userRepository,
                gameAccountRepository,
                discountService,
                eventPublisher
        );

        tradeMatchingService = new TradeMatchingServiceImpl(
                orderRepository,
                orderItemRepository,
                cardInventoryRepository,
                gameAccountRepository
        );

        customer1 = User.builder().id(1L).username("customer_one").role(UserRole.CUSTOMER).build();
        customer2 = User.builder().id(2L).username("customer_two").role(UserRole.CUSTOMER).build();
        staffUser = User.builder().id(99L).username("staff_admin").role(UserRole.STAFF).build();

        charizard = Card.builder().id(10L).name("Charizard ex").cardNumber("006/165").build();

        accountA = GameAccount.builder()
                .id(100L)
                .accountCode("VAULT-ACC-A")
                .inGameName("Trainer_A")
                .friendId("1111-1111-1111-1111")
                .tradeStatus(AccountTradeStatus.READY)
                .build();

        accountB = GameAccount.builder()
                .id(200L)
                .accountCode("VAULT-ACC-B")
                .inGameName("Trainer_B")
                .friendId("2222-2222-2222-2222")
                .tradeStatus(AccountTradeStatus.READY)
                .build();

        inventoryA = CardInventory.builder()
                .id(1001L)
                .card(charizard)
                .gameAccount(accountA)
                .quantity(1)
                .sellingPrice(new BigDecimal("1200.00"))
                .build();

        inventoryB = CardInventory.builder()
                .id(1002L)
                .card(charizard)
                .gameAccount(accountB)
                .quantity(3)
                .sellingPrice(new BigDecimal("1200.00"))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(User user, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("1. การจองใบสุดท้ายแล้วยังจับคู่ได้ (Last Card Matching)")
    class LastCardMatchingTests {

        @Test
        @DisplayName("จองการ์ดใบสุดท้าย (สต็อกคงเหลือกลายเป็น 0) แต่ auto-match ยังสามารถจับคู่กับบัญชีที่ถือการจองได้สำเร็จ")
        void bookLastCard_StillCanAutoMatchSuccessfully() {
            // จำลองการจองการ์ดใบเดียวในคลัง A: หักสต็อก 1 เหลือ 0
            inventoryA.deductStock(1);
            assertThat(inventoryA.getQuantity()).isEqualTo(0);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryA)
                    .assignedAccount(accountA)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                    .unitPrice(new BigDecimal("1200.00"))
                    .subtotal(new BigDecimal("1200.00"))
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(item));
            when(cardInventoryRepository.findByCardId(10L)).thenReturn(List.of(inventoryA));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

            // เรียก auto-match: แม้สต็อกคงเหลือพร้อมขายเป็น 0 แต่เพราะการจองถูกถือไว้ที่คลังนี้อยู่แล้ว ต้องจับคู่สำเร็จได้
            TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(501L);

            assertThat(response).isNotNull();
            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(100L);
            assertThat(item.getAssignedAccount()).isEqualTo(accountA);
            assertThat(item.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);
            verify(orderItemRepository).save(item);
        }
    }

    @Nested
    @DisplayName("2. จองหลายรายการแล้วรายการหนึ่งล้มเหลว (Multi-item Abort & Rollback Invariant)")
    class MultiItemRollbackTests {

        @Test
        @DisplayName("เมื่อสั่งซื้อ 2 รายการ แต่รายการที่สองสต็อกไม่พอ ต้องโยน InsufficientStockException และไม่บันทึก Order")
        void multiItemBooking_SecondItemFails_AbortsAndNeverSavesOrder() {
            CardInventory inv1 = CardInventory.builder().id(1L).card(charizard).quantity(5).sellingPrice(new BigDecimal("100.00")).build();
            CardInventory inv2 = CardInventory.builder().id(2L).card(charizard).quantity(0).sellingPrice(new BigDecimal("200.00")).build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(customer1));
            when(cardInventoryRepository.findById(1L)).thenReturn(Optional.of(inv1));
            when(cardInventoryRepository.findById(2L)).thenReturn(Optional.of(inv2));

            PlaceOrderRequest request = PlaceOrderRequest.builder()
                    .userId(1L)
                    .customerFriendId("1234-5678-9012-3456")
                    .items(List.of(
                            PlaceOrderRequest.OrderItemRequest.builder().inventoryId(1L).quantity(1).build(),
                            PlaceOrderRequest.OrderItemRequest.builder().inventoryId(2L).quantity(1).build()
                    ))
                    .build();

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(InsufficientStockException.class);

            verify(orderRepository, never()).save(any(Order.class));
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("3. เปลี่ยนบัญชีแล้วจำนวนและสภาพการ์ดทั้งสองคลังถูกต้อง (Condition & Stock Preservation on Reassign)")
    class ReassignAccountTests {

        @Test
        @DisplayName("ย้ายไปคลัง MINT ที่เพียงพอ: คืนคลังเดิมและหักคลังใหม่ถูกต้อง พร้อมรักษาการ์ดและสภาพ MINT")
        void reassignAccount_TargetAccountHasSufficientMint_TransfersStockCleanly() {
            // เริ่มต้น item ถือการจองจาก Inventory A (MINT, สต็อกเดิม 1 หักไปแล้วเหลือ 0)
            inventoryA.setCondition(CardCondition.MINT);
            inventoryA.setQuantity(0);

            inventoryB.setCondition(CardCondition.MINT);
            inventoryB.setQuantity(3);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryA)
                    .assignedAccount(accountA)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                    .unitPrice(new BigDecimal("1200.00"))
                    .subtotal(new BigDecimal("1200.00"))
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .finalAmount(new BigDecimal("1200.00"))
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(gameAccountRepository.findById(200L)).thenReturn(Optional.of(accountB));
            when(cardInventoryRepository.findByCardId(10L)).thenReturn(List.of(inventoryA, inventoryB));

            // สั่งย้ายบัญชีไปยัง Account B ผ่าน OrderService
            OrderItemResponse response = orderService.reassignOrderItemAccount(101L, 501L, 200L);

            assertThat(response).isNotNull();
            // คลังเดิม A ต้องได้รับสต็อกคืน (+1 กลายเป็น 1)
            assertThat(inventoryA.getQuantity()).isEqualTo(1);
            // คลังใหม่ B ต้องถูกหักสต็อก (-1 กลายเป็น 2)
            assertThat(inventoryB.getQuantity()).isEqualTo(2);
            // item ต้องชี้ไปยังคลังใหม่ B และสภาพยังเป็น MINT
            assertThat(item.getInventory()).isEqualTo(inventoryB);
            assertThat(item.getInventory().getCondition()).isEqualTo(CardCondition.MINT);
            assertThat(item.getAssignedAccount()).isEqualTo(accountB);
            assertThat(item.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);

            verify(cardInventoryRepository).save(inventoryA);
            verify(cardInventoryRepository).save(inventoryB);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("จอง MINT แต่บัญชีอื่นมีเฉพาะ PLAYED: ห้ามย้ายไปบัญชีนั้น (โยน InsufficientStockException)")
        void reassignAccount_TargetAccountOnlyHasPlayed_ThrowsExceptionAndRejects() {
            inventoryA.setCondition(CardCondition.MINT);
            inventoryA.setQuantity(0);

            // บัญชี B มีการ์ดใบเดียวกัน แต่เป็นสภาพ PLAYED เท่านั้น
            inventoryB.setCondition(CardCondition.PLAYED);
            inventoryB.setQuantity(5);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryA)
                    .assignedAccount(accountA)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                    .unitPrice(new BigDecimal("1200.00"))
                    .subtotal(new BigDecimal("1200.00"))
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(gameAccountRepository.findById(200L)).thenReturn(Optional.of(accountB));
            when(cardInventoryRepository.findByCardId(10L)).thenReturn(List.of(inventoryA, inventoryB));

            // ต้องโยน InsufficientStockException ห้ามย้ายข้ามสภาพการ์ด
            assertThatThrownBy(() -> orderService.reassignOrderItemAccount(101L, 501L, 200L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("MINT");

            // สต็อกทั้งสองคลังต้องไม่เปลี่ยนแปลง
            assertThat(inventoryA.getQuantity()).isEqualTo(0);
            assertThat(inventoryB.getQuantity()).isEqualTo(5);
            // item ต้องยังผูกอยู่กับคลังเดิม A
            assertThat(item.getInventory()).isEqualTo(inventoryA);
            assertThat(item.getAssignedAccount()).isEqualTo(accountA);

            verify(cardInventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("ย้ายล้มเหลวเนื่องจากสต็อกไม่พอ: จำนวนทุกคลังต้องคงเดิม")
        void reassignAccount_InsufficientStock_FailsAndAllInventoriesUnchanged() {
            inventoryA.setCondition(CardCondition.MINT);
            inventoryA.setQuantity(0);

            // บัญชี B มี MINT แต่มี 0 ใบ
            inventoryB.setCondition(CardCondition.MINT);
            inventoryB.setQuantity(0);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryA)
                    .assignedAccount(accountA)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(gameAccountRepository.findById(200L)).thenReturn(Optional.of(accountB));
            when(cardInventoryRepository.findByCardId(10L)).thenReturn(List.of(inventoryA, inventoryB));

            assertThatThrownBy(() -> orderService.reassignOrderItemAccount(101L, 501L, 200L))
                    .isInstanceOf(InsufficientStockException.class);

            // จำนวนคงเดิม ไม่มีการโอนย้าย
            assertThat(inventoryA.getQuantity()).isEqualTo(0);
            assertThat(inventoryB.getQuantity()).isEqualTo(0);
            assertThat(item.getInventory()).isEqualTo(inventoryA);
            verify(cardInventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("ยกเลิกหลังย้าย: คืนสต็อกไปยังคลังที่ถือการจองล่าสุด (คลัง B)")
        void cancelOrder_AfterReassign_RestoresStockToLatestHoldingInventory() {
            // สมมติย้ายมาอยู่ที่ Inventory B แล้ว (B มี 2 หลังถูกหัก 1 จากเดิม 3)
            inventoryA.setQuantity(1);
            inventoryB.setQuantity(2);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryB)
                    .assignedAccount(accountB)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // สั่งยกเลิกออเดอร์
            OrderResponse response = orderService.transitionOrderStatus(101L, "cancel");

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            // คลังล่าสุด B ต้องได้รับสต็อกคืน (+1 กลายเป็น 3)
            assertThat(inventoryB.getQuantity()).isEqualTo(3);
            // คลังเดิม A ต้องไม่ถูกคืนซ้ำ (ยังคงเป็น 1 เท่าเดิม)
            assertThat(inventoryA.getQuantity()).isEqualTo(1);

            verify(cardInventoryRepository).save(inventoryB);
        }

        @Test
        @DisplayName("ราคาต่อหน่วยและยอดออเดอร์คงเดิมหลังย้าย: ไม่เปลี่ยนตามราคาของคลังใหม่")
        void reassignAccount_PreservesOriginalUnitPriceAndTotalAmount() {
            inventoryA.setSellingPrice(new BigDecimal("1200.00"));
            inventoryA.setQuantity(0);

            // คลัง B ขายแพงกว่า (2500.00)
            inventoryB.setSellingPrice(new BigDecimal("2500.00"));
            inventoryB.setQuantity(3);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryA)
                    .assignedAccount(accountA)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                    .unitPrice(new BigDecimal("1200.00"))
                    .subtotal(new BigDecimal("1200.00"))
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(new BigDecimal("1200.00"))
                    .finalAmount(new BigDecimal("1200.00"))
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(gameAccountRepository.findById(200L)).thenReturn(Optional.of(accountB));
            when(cardInventoryRepository.findByCardId(10L)).thenReturn(List.of(inventoryA, inventoryB));

            orderService.reassignOrderItemAccount(101L, 501L, 200L);

            // ราคาที่ฟรีซไว้ตอนซื้อ (unitPrice, subtotal, finalAmount) ต้องไม่เปลี่ยนตามราคาคลัง B
            assertThat(item.getUnitPrice()).isEqualByComparingTo("1200.00");
            assertThat(item.getSubtotal()).isEqualByComparingTo("1200.00");
            assertThat(order.getTotalAmount()).isEqualByComparingTo("1200.00");
            assertThat(order.getFinalAmount()).isEqualByComparingTo("1200.00");
        }
    }

    @Nested
    @DisplayName("4. ยกเลิกคืนสต็อกครั้งเดียว (Single Stock Restoration to Latest Inventory)")
    class CancelOrderRestorationTests {

        @Test
        @DisplayName("ยกเลิกออเดอร์: คืนสต็อกไปยังคลังที่ถือการจองล่าสุด และพยายามยกเลิกซ้ำจะถูกปฏิเสธ")
        void cancelOrder_RestoresStockOnceToLatestHoldingInventory() {
            // สมมติ item ถูกย้ายมาถือที่ Inventory B แล้ว (B มี 2 หลังถูกหัก 1 จากเดิม 3)
            inventoryA.setQuantity(1);
            inventoryB.setQuantity(2);

            OrderItem item = OrderItem.builder()
                    .id(501L)
                    .inventory(inventoryB)
                    .assignedAccount(accountB)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                    .build();

            Order order = Order.builder()
                    .id(101L)
                    .orderCode("ORD-2026-001")
                    .orderStatus(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();
            item.setOrder(order);

            when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // ยกเลิกคำสั่งซื้อ
            OrderResponse response = orderService.transitionOrderStatus(101L, "cancel");

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            // คลัง B ต้องได้รับสต็อกคืน (+1 กลายเป็น 3)
            assertThat(inventoryB.getQuantity()).isEqualTo(3);
            // คลัง A ต้องไม่ถูกแตะต้อง (ยังคงเป็น 1)
            assertThat(inventoryA.getQuantity()).isEqualTo(1);
            verify(cardInventoryRepository).save(inventoryB);

            // พยายามยกเลิกซ้ำบนออเดอร์เดิม ต้อง Fail-Fast และไม่คืนสต็อกซ้ำ
            assertThatThrownBy(() -> orderService.transitionOrderStatus(101L, "cancel"))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot cancel order in current state: CANCELLED");

            // สต็อกคลัง B ต้องยังคงเป็น 3 ไม่เพิ่มเป็น 4
            assertThat(inventoryB.getQuantity()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("5. การตรวจสอบสิทธิ์เจ้าของออเดอร์ (Ownership & Authorization)")
    class OwnershipSecurityTests {

        @Test
        @DisplayName("ลูกค้า (CUSTOMER) ไม่สามารถอ่านข้อมูลออเดอร์ของลูกค้ารายอื่นได้ -> 403 AccessDeniedException")
        void customerCannotViewOtherCustomerOrder() {
            Order otherOrder = Order.builder()
                    .id(202L)
                    .user(customer2)
                    .orderCode("ORD-2026-CUSTOMER2")
                    .build();

            when(orderRepository.findById(202L)).thenReturn(Optional.of(otherOrder));

            // ล็อกอินเป็น customer1
            authenticateUser(customer1, "CUSTOMER");

            assertThatThrownBy(() -> orderService.getOrderById(202L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("You do not have permission to view this order");
        }

        @Test
        @DisplayName("ลูกค้า (CUSTOMER) ไม่สามารถสร้างออเดอร์โดยระบุ userId ของคนอื่นได้ -> 403 AccessDeniedException")
        void customerCannotPlaceOrderForOtherUser() {
            authenticateUser(customer1, "CUSTOMER");
            when(userRepository.findByUsername("customer_one")).thenReturn(Optional.of(customer1));

            PlaceOrderRequest request = PlaceOrderRequest.builder()
                    .userId(2L) // พยายามส่ง userId ของ customer2
                    .customerFriendId("1234-5678-9012-3456")
                    .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(1001L).quantity(1).build()))
                    .build();

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Customers can only place orders for themselves");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("เจ้าหน้าที่ (STAFF/ADMIN) สามารถดูออเดอร์ของลูกค้าคนใดก็ได้")
        void staffCanViewAnyCustomerOrder() {
            Order customerOrder = Order.builder()
                    .id(101L)
                    .user(customer1)
                    .orderCode("ORD-2026-001")
                    .build();

            when(orderRepository.findById(101L)).thenReturn(Optional.of(customerOrder));

            // ล็อกอินเป็น staff
            authenticateUser(staffUser, "STAFF");

            OrderResponse response = orderService.getOrderById(101L);
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("เจ้าหน้าที่ (STAFF/ADMIN) สามารถจองการ์ดแทนลูกค้า (Delegated Booking) ได้")
        void staffCanPlaceOrderOnBehalfOfCustomer() {
            authenticateUser(staffUser, "STAFF");

            when(userRepository.findById(1L)).thenReturn(Optional.of(customer1));
            when(cardInventoryRepository.findById(1001L)).thenReturn(Optional.of(inventoryA));
            when(orderRepository.count()).thenReturn(0L);
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order ord = inv.getArgument(0);
                ord.setId(101L);
                return ord;
            });

            PlaceOrderRequest request = PlaceOrderRequest.builder()
                    .userId(1L) // จองให้ customer1
                    .customerFriendId("1234-5678-9012-3456")
                    .items(List.of(PlaceOrderRequest.OrderItemRequest.builder().inventoryId(1001L).quantity(1).build()))
                    .build();

            OrderResponse response = orderService.createOrder(request);
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(101L);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("ลูกค้า (CUSTOMER) ดึง getAllOrders จะได้เฉพาะออเดอร์ของตัวเองเท่านั้น")
        void customerGetAllOrders_ReturnsOnlyOwnOrders() {
            authenticateUser(customer1, "CUSTOMER");
            when(userRepository.findByUsername("customer_one")).thenReturn(Optional.of(customer1));

            Order ownOrder = Order.builder().id(101L).user(customer1).orderCode("ORD-2026-001").build();
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(ownOrder));

            List<OrderResponse> orders = orderService.getAllOrders();

            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getId()).isEqualTo(101L);
            verify(orderRepository, never()).findAll();
        }
    }
}
