package com.pokevault.modules.order;

import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.dto.OrderItemResponse;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
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
        @DisplayName("updateItemTradeStatus: อัปเดตสถานะเทรดของไอเทมสำเร็จ และ auto-sync เป็น SHIPPING เมื่อส่งการ์ด")
        void updateItemTradeStatus_Success_AndSyncShipping() {
                OrderItem item = OrderItem.builder()
                                .id(10L)
                                .quantity(1)
                                .tradeStatus(TradeFulfillmentStatus.FRIEND_PENDING)
                                .build();

                Order order = Order.builder()
                                .id(1L)
                                .orderCode("ORD-2026-001")
                                .orderStatus(OrderStatus.PAID)
                                .build();
                order.addItem(item);

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 10L,
                                TradeFulfillmentStatus.TRADE_SENT);

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(10L);
                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.TRADE_SENT);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("updateItemTradeStatus: เมื่อทุกไอเทมส่งมอบเสร็จสิ้น (COMPLETED) auto-sync OrderStatus เป็น COMPLETED")
        void updateItemTradeStatus_AllItemsCompleted_SyncsOrderCompleted() {
                OrderItem item1 = OrderItem.builder().id(10L).quantity(1).tradeStatus(TradeFulfillmentStatus.COMPLETED)
                                .build();
                OrderItem item2 = OrderItem.builder().id(20L).quantity(1).tradeStatus(TradeFulfillmentStatus.TRADE_SENT)
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

                // อัปเดตไอเทมชิ้นสุดท้ายให้ COMPLETED
                OrderItemResponse response = orderService.updateItemTradeStatus(1L, 20L,
                                TradeFulfillmentStatus.COMPLETED);

                assertThat(response.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.COMPLETED);
                assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
                verify(orderRepository, times(1)).save(order);
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน ResourceNotFoundException เมื่อไม่พบ Order ID")
        void updateItemTradeStatus_OrderNotFound_ThrowsException() {
                when(orderRepository.findById(999L)).thenReturn(Optional.empty());

                assertThatThrownBy(
                                () -> orderService.updateItemTradeStatus(999L, 10L, TradeFulfillmentStatus.COMPLETED))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Order");

                verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateItemTradeStatus: โยน ResourceNotFoundException เมื่อไม่พบ Item ID ในออเดอร์นั้น")
        void updateItemTradeStatus_ItemNotFound_ThrowsException() {
                Order order = Order.builder().id(1L).orderCode("ORD-2026-001").build();

                when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

                assertThatThrownBy(() -> orderService.updateItemTradeStatus(1L, 999L, TradeFulfillmentStatus.COMPLETED))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("OrderItem");

                verify(orderRepository, never()).save(any());
        }

}
