package com.pokevault.modules.order;

import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.dto.OrderItemResponse;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
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
class OrderServiceTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private CardInventoryRepository cardInventoryRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private DiscountService discountService;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        @InjectMocks
        private OrderServiceImpl orderService;

        private User sampleUser;
        private UserProfile sampleProfile;
        private Card sampleCard;
        private CardInventory sampleInventory;
        private PlaceOrderRequest sampleRequest;

        @BeforeEach
        void setUp() {
                sampleUser = User.builder()
                                .id(1L)
                                .username("ash_ketchum")
                                .email("ash@pallet.town")
                                .build();

                sampleProfile = UserProfile.builder()
                                .id(1L)
                                .user(sampleUser)
                                .membershipTier(MembershipTier.VIP)
                                .build();
                sampleUser.setUserProfile(sampleProfile);

                sampleCard = Card.builder()
                                .id(10L)
                                .cardNumber("095/226")
                                .name("Pikachu ex")
                                .build();

                sampleInventory = CardInventory.builder()
                                .id(100L)
                                .card(sampleCard)
                                .condition(CardCondition.MINT)
                                .quantity(5)
                                .sellingPrice(new BigDecimal("990.00"))
                                .build();

                PlaceOrderRequest.OrderItemRequest itemRequest = PlaceOrderRequest.OrderItemRequest.builder()
                                .inventoryId(100L)
                                .quantity(1)
                                .build();

                sampleRequest = PlaceOrderRequest.builder()
                                .userId(1L)
                                .customerFriendId("1234-5678-9012-3456")
                                .customerInGameName("AshKetchum")
                                .items(List.of(itemRequest))
                                .notes("Trade at 8 PM")
                                .build();
        }

        @Test
        @DisplayName("Should create order successfully with stock deduction, VIP discount, and event publishing")
        void createOrder_Success() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
                when(cardInventoryRepository.findById(100L)).thenReturn(Optional.of(sampleInventory));
                when(discountService.calculateDiscount(eq(MembershipTier.VIP), any(BigDecimal.class)))
                                .thenReturn(new BigDecimal("99.00"));
                when(orderRepository.count()).thenReturn(0L);

                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                        Order order = invocation.getArgument(0);
                        order.setId(1L);
                        return order;
                });

                OrderResponse response = orderService.createOrder(sampleRequest);

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(1L);
                assertThat(response.getOrderCode()).isEqualTo(String.format("ORD-%d-001", LocalDate.now().getYear()));
                assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
                assertThat(response.getCustomerFriendId()).isEqualTo("1234-5678-9012-3456");
                assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("990.00"));
                assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("99.00"));
                assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("891.00"));

                // Verify stock deducted
                assertThat(sampleInventory.getQuantity()).isEqualTo(4);
                verify(cardInventoryRepository, times(1)).save(sampleInventory);

                // Verify order saved
                verify(orderRepository, times(1)).save(any(Order.class));

                // Verify OrderPlacedEvent published
                ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
                verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
                OrderPlacedEvent publishedEvent = eventCaptor.getValue();
                assertThat(publishedEvent.getOrderId()).isEqualTo(1L);
                assertThat(publishedEvent.getOrderCode())
                                .isEqualTo(String.format("ORD-%d-001", LocalDate.now().getYear()));
                assertThat(publishedEvent.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should throw InsufficientStockException when inventory stock is lower than requested")
        void createOrder_InsufficientStock_ThrowsException() {
                sampleInventory.setQuantity(1);
                sampleRequest.getItems().get(0).setQuantity(5);

                when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
                when(cardInventoryRepository.findById(100L)).thenReturn(Optional.of(sampleInventory));

                assertThatThrownBy(() -> orderService.createOrder(sampleRequest))
                                .isInstanceOf(InsufficientStockException.class)
                                .hasMessageContaining("Insufficient stock");

                // Verify stock was not deducted and order not saved
                assertThat(sampleInventory.getQuantity()).isEqualTo(1);
                verify(orderRepository, never()).save(any(Order.class));
                verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not found")
        void createOrder_UserNotFound_ThrowsException() {
                when(userRepository.findById(1L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.createOrder(sampleRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("User");

                verify(cardInventoryRepository, never()).findById(any());
                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when inventory card is not found")
        void createOrder_InventoryNotFound_ThrowsException() {
                when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
                when(cardInventoryRepository.findById(100L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.createOrder(sampleRequest))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("CardInventory");

                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return order response by ID when order exists")
        void getOrderById_Success() {
                Order sampleOrder = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .user(sampleUser)
                                .orderStatus(OrderStatus.PENDING)
                                .totalAmount(new BigDecimal("990.00"))
                                .discountAmount(new BigDecimal("99.00"))
                                .finalAmount(new BigDecimal("891.00"))
                                .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

                OrderResponse response = orderService.getOrderById(1L);

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(1L);
                assertThat(response.getOrderCode()).isEqualTo("ORD-2026-001");
                verify(orderRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when order ID does not exist")
        void getOrderById_NotFound_ThrowsException() {
                when(orderRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.getOrderById(999L))
                                .isInstanceOf(ResourceNotFoundException.class);

                verify(orderRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("Should return all orders")
        void getAllOrders_ReturnsList() {
                Order order1 = Order.builder().id(1L).orderCode("ORD-2026-001").user(sampleUser).build();
                Order order2 = Order.builder().id(2L).orderCode("ORD-2026-002").user(sampleUser).build();

                when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

                List<OrderResponse> responses = orderService.getAllOrders();

                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).getOrderCode()).isEqualTo("ORD-2026-001");
                assertThat(responses.get(1).getOrderCode()).isEqualTo("ORD-2026-002");
                verify(orderRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("updateItemTradeStatus: อัปเดตเป็น TRADE_SENT สำเร็จเมื่อจับคู่บัญชีแล้วและออเดอร์อยู่ใน SHIPPING")
        void updateItemTradeStatus_Success_TradeSent() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT);

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(10L);
                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.TRADE_SENT);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("updateItemTradeStatus: เมื่อทุกรายการเทรดเสร็จสิ้น (COMPLETED) เปลี่ยน Order เป็น COMPLETED ผ่าน State Pattern")
        void updateItemTradeStatus_AllItemsCompleted_SyncsOrderCompletedViaStatePattern() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item1 = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.COMPLETED)
                                .build();
                OrderItem item2 = OrderItem.builder()
                                .id(20L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.TRADE_SENT)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item1);
                order.addItem(item2);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // อัปเดตไอเทมชิ้นที่สองให้ COMPLETED
                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 20L, TradeFulfillmentStatus.COMPLETED);

                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.COMPLETED);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("updateItemTradeStatus: ออเดอร์หลายรายการยังคงเป็น SHIPPING หากบางรายการยังไม่เสร็จ")
        void updateItemTradeStatus_MultiItem_RemainsShippingWhenPartiallyCompleted() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item1 = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();
                OrderItem item2 = OrderItem.builder()
                                .id(20L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item1);
                order.addItem(item2);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT);

                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.TRADE_SENT);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("updateItemTradeStatus: Idempotent - กดส่งสถานะเดิมซ้ำ ให้คืนสถานะปัจจุบันโดยไม่บันทึกซ้ำ")
        void updateItemTradeStatus_Idempotent_ReturnsCurrentWithoutModification() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.TRADE_SENT)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                // ส่ง TRADE_SENT ซ้ำ
                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT);

                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.TRADE_SENT);
                // ต้องไม่เรียก save เมื่อสถานะตรงกันอยู่แล้ว
                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน IllegalArgumentException เมื่อส่งสถานะที่ไม่รองรับ เช่น UNASSIGNED หรือ FRIEND_PENDING")
        void updateItemTradeStatus_UnsupportedStatus_ThrowsException() {
                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.UNASSIGNED))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Unsupported trade status");

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.FRIEND_PENDING))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Unsupported trade status");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อไอเทมยังไม่ผูกบัญชีเกม (assignedAccount == null)")
        void updateItemTradeStatus_UnassignedAccount_ThrowsConflict() {
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(null)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("Game account has not been assigned");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อออเดอร์ยังไม่อยู่ในสถานะ SHIPPING (เช่น PAID หรือ PENDING)")
        void updateItemTradeStatus_OrderNotInShipping_ThrowsConflict() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.PAID)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("Order must be in SHIPPING status");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อออเดอร์ถูกยกเลิก (CANCELLED) หรือจบแล้ว (COMPLETED)")
        void updateItemTradeStatus_TerminalOrder_ThrowsConflict() {
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order cancelledOrder = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.CANCELLED)
                                .build();
                cancelledOrder.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(cancelledOrder));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("terminal state");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อข้ามขั้นจาก UNASSIGNED ไป TRADE_SENT")
        void updateItemTradeStatus_SkipSequence_UnassignedToTradeSent_ThrowsConflict() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("Expected: FRIEND_PENDING");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อข้ามขั้นจาก FRIEND_PENDING ไป COMPLETED โดยไม่ผ่าน TRADE_SENT")
        void updateItemTradeStatus_SkipSequence_FriendPendingToCompleted_ThrowsConflict() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.COMPLETED))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("Expected: TRADE_SENT");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน TradeStateConflictException เมื่อพยายามย้อนสถานะจาก COMPLETED กลับเป็น TRADE_SENT")
        void updateItemTradeStatus_ReverseSequence_CompletedToTradeSent_ThrowsConflict() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.COMPLETED)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("Expected: FRIEND_PENDING");
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน ResourceNotFoundException (404) เมื่อไม่พบ Order ID")
        void updateItemTradeStatus_OrderNotFound_ThrowsException() {
                when(orderRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(999L, 10L, TradeFulfillmentStatus.COMPLETED))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Order");

                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน ResourceNotFoundException (404) เมื่อส่ง OrderItem ที่อยู่คนละออเดอร์ (IDOR Protection)")
        void updateItemTradeStatus_ItemNotFoundInOrder_ThrowsException() {
                Order order = Order.builder().id(1L).orderCode("ORD-2026-001").build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 999L, TradeFulfillmentStatus.COMPLETED))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("OrderItem");

                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("transitionOrderStatus: complete สำเร็จเมื่อออเดอร์อยู่ใน SHIPPING และทุก OrderItem เป็น COMPLETED")
        void transitionOrderStatus_Complete_Success() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.COMPLETED)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                OrderResponse response = orderService.transitionOrderStatus(1L, "complete");

                assertThat(response).isNotNull();
                assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("transitionOrderStatus: โยน TradeStateConflictException เมื่อปิดออเดอร์ (complete) ขณะยังมีรายการที่ยังเทรดไม่เสร็จ")
        void transitionOrderStatus_Complete_IncompleteItems_ThrowsConflict() {
                GameAccount account = GameAccount.builder().id(100L).accountCode("ACC-001").build();
                OrderItem item1 = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.COMPLETED)
                                .build();
                OrderItem item2 = OrderItem.builder()
                                .id(20L)
                                .quantity(1)
                                .assignedAccount(account)
                                .tradeStatus(TradeFulfillmentStatus.TRADE_SENT)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();
                order.addItem(item1);
                order.addItem(item2);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.transitionOrderStatus(1L, "complete"))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("not all items have reached COMPLETED trade status");

                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("transitionOrderStatus: โยน TradeStateConflictException เมื่อปิดออเดอร์ (complete) แต่ออเดอร์ไม่มีรายการสินค้า")
        void transitionOrderStatus_Complete_NoItems_ThrowsConflict() {
                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.SHIPPING)
                                .build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.transitionOrderStatus(1L, "complete"))
                                .isInstanceOf(TradeStateConflictException.class)
                                .hasMessageContaining("order has no items");

                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
                verify(orderRepository, never()).save(any());
        }

}
