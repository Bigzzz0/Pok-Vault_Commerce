package com.pokevault.modules.order.service;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.modules.trade.state.OrderContext;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final CardInventoryRepository cardInventoryRepository;
        private final UserRepository userRepository;
        private final DiscountService discountService;
        private final ApplicationEventPublisher eventPublisher;

        @Override
        public OrderResponse createOrder(PlaceOrderRequest request) {
                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

                MembershipTier tier = MembershipTier.REGULAR;
                if (user.getUserProfile() != null && user.getUserProfile().getMembershipTier() != null) {
                        tier = user.getUserProfile().getMembershipTier();
                }

                Order order = Order.builder()
                                .user(user)
                                .customerFriendId(request.getCustomerFriendId())
                                .customerInGameName(request.getCustomerInGameName())
                                .orderStatus(OrderStatus.PENDING)
                                .notes(request.getNotes())
                                .totalAmount(BigDecimal.ZERO)
                                .discountAmount(BigDecimal.ZERO)
                                .finalAmount(BigDecimal.ZERO)
                                .build();

                BigDecimal subtotal = BigDecimal.ZERO;

                for (PlaceOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                        CardInventory inventory = cardInventoryRepository.findById(itemReq.getInventoryId())
                                        .orElseThrow(() -> new ResourceNotFoundException("CardInventory", "id",
                                                        itemReq.getInventoryId()));

                        if (!inventory.hasSufficientStock(itemReq.getQuantity())) {
                                String cardName = inventory.getCard() != null ? inventory.getCard().getName()
                                                : "ID " + inventory.getId();
                                throw new InsufficientStockException("Insufficient stock for card: " + cardName
                                                + " (Available: " + inventory.getQuantity() + ", Requested: "
                                                + itemReq.getQuantity() + ")");
                        }

                        // Deduct stock immediately
                        inventory.deductStock(itemReq.getQuantity());
                        cardInventoryRepository.save(inventory);

                        BigDecimal unitPrice = inventory.getSellingPrice() != null ? inventory.getSellingPrice()
                                        : BigDecimal.ZERO;
                        BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                                        .setScale(2, RoundingMode.HALF_UP);
                        subtotal = subtotal.add(itemSubtotal);

                        OrderItem orderItem = OrderItem.builder()
                                        .inventory(inventory)
                                        .assignedAccount(inventory.getGameAccount())
                                        .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                                        .quantity(itemReq.getQuantity())
                                        .unitPrice(unitPrice)
                                        .subtotal(itemSubtotal)
                                        .build();

                        order.addItem(orderItem);
                }

                // Apply Strategy Pattern for discount calculation
                BigDecimal discountAmount = discountService.calculateDiscount(tier, subtotal);
                BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2,
                                RoundingMode.HALF_UP);

                order.setOrderCode(generateOrderCode());
                order.setTotalAmount(subtotal.setScale(2, RoundingMode.HALF_UP));
                order.setDiscountAmount(discountAmount);
                order.setFinalAmount(finalAmount);

                Order savedOrder = orderRepository.save(order);

                // Publish OrderPlacedEvent via Spring ApplicationEventPublisher
                eventPublisher.publishEvent(OrderPlacedEvent.builder()
                                .orderId(savedOrder.getId())
                                .orderCode(savedOrder.getOrderCode())
                                .items(savedOrder.getItems())
                                .timestamp(LocalDateTime.now())
                                .build());

                return OrderResponse.fromEntity(savedOrder);
        }

        @Override
        public OrderResponse transitionOrderStatus(Long id, String action) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

                log.info("Transitioning order id: {} from current status: {} with action: {}",
                                id, order.getOrderStatus(), action);

                // 1. นำ Entity เข้าสู่ OrderContext ของ GoF State Pattern
                OrderContext context = OrderContext.fromOrder(order);

                // 2. สั่งรัน Action (ถ้าผิดกฎ State Pattern จะ Fail-Fast โยน
                // InvalidOrderStateException ทันที)
                context.executeAction(action);

                // 3. บันทึก Entity ที่อัปเดตสถานะใหม่ลงฐานข้อมูล
                Order savedOrder = orderRepository.save(order);
                log.info("Order id: {} successfully transitioned to status: {}", id, savedOrder.getOrderStatus());

                return OrderResponse.fromEntity(savedOrder);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponse getOrderById(Long id) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
                return OrderResponse.fromEntity(order);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderResponse> getAllOrders() {
                return orderRepository.findAll().stream()
                                .map(OrderResponse::fromEntity)
                                .toList();
        }

        @Override
        public OrderItemResponse updateItemTradeStatus(Long orderId, Long itemId, TradeFulfillmentStatus status) {
                // 1. ค้นหา Order
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

                // 2. ค้นหา OrderItem ภายใน Order นั้น (ป้องกัน IDOR / เข้าถึงไอเทมข้ามออเดอร์)
                OrderItem targetItem = order.getItems().stream()
                                .filter(item -> item.getId().equals(itemId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", itemId));

                // 3. อัปเดตสถานะการเทรดของไอเทม
                targetItem.setTradeStatus(status);
                log.info("Updated trade status for order item [{}] in order [{}] to {}",
                                itemId, order.getOrderCode(), status);

                // 4. Auto-sync: ตรวจสอบและอัปเดตสถานะ Order ภาพรวม
                boolean allItemsCompleted = order.getItems().stream()
                                .allMatch(item -> item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED);
                boolean anyItemTradeSent = order.getItems().stream()
                                .anyMatch(item -> item.getTradeStatus() == TradeFulfillmentStatus.TRADE_SENT);

                if (allItemsCompleted && !order.getItems().isEmpty()) {
                        if (order.getOrderStatus() == OrderStatus.SHIPPING
                                        || order.getOrderStatus() == OrderStatus.PAID) {
                                order.setOrderStatus(OrderStatus.COMPLETED);
                                log.info("All trade items completed in order [{}], auto-updated OrderStatus to COMPLETED",
                                                order.getOrderCode());
                        }
                } else if (anyItemTradeSent) {
                        if (order.getOrderStatus() == OrderStatus.PAID) {
                                order.setOrderStatus(OrderStatus.SHIPPING);
                                log.info("Item trade sent in order [{}], auto-updated OrderStatus to SHIPPING",
                                                order.getOrderCode());
                        }
                }

                orderRepository.save(order);
                return OrderItemResponse.fromEntity(targetItem);
        }

        private String generateOrderCode() {
                long count = orderRepository.count() + 1;
                return String.format("ORD-%d-%03d", LocalDate.now().getYear(), count);
        }
}
