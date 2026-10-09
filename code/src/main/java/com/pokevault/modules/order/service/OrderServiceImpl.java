package com.pokevault.modules.order.service;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.modules.trade.state.OrderContext;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        private final GameAccountRepository gameAccountRepository;
        private final DiscountService discountService;
        private final ApplicationEventPublisher eventPublisher;

        @Override
        public OrderResponse createOrder(PlaceOrderRequest request) {
                // 1. ตรวจสอบสิทธิ์เจ้าของคำสั่งซื้อร่วมกับ Security Context
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
                        if (!isStaffOrAdmin) {
                                // ลูกค้าทั่วไปสร้างออเดอร์ให้ตัวเองเท่านั้น ไม่เชื่อถือ userId จาก request ข้ามบัญชี
                                User currentUser = userRepository.findByUsername(auth.getName())
                                                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found: " + auth.getName()));
                                if (request.getUserId() != null && !request.getUserId().equals(currentUser.getId())) {
                                        throw new AccessDeniedException("Customers can only place orders for themselves");
                                }
                                request.setUserId(currentUser.getId());
                        }
                }

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

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
                        if (!isStaffOrAdmin) {
                                if (order.getUser() != null && !order.getUser().getUsername().equals(auth.getName())) {
                                        throw new AccessDeniedException("Access denied: You do not have permission to modify this order");
                                }
                        }
                }

                log.info("Transitioning order id: {} from current status: {} with action: {}",
                                id, order.getOrderStatus(), action);

                // 1. นำ Entity เข้าสู่ OrderContext ของ GoF State Pattern
                OrderContext context = OrderContext.fromOrder(order);

                // 2. สั่งรัน Action (ถ้าผิดกฎ State Pattern จะ Fail-Fast โยน
                // InvalidOrderStateException ทันที)
                context.executeAction(action);

                // 3. หากเป็นการยกเลิก บันทึกคืนสต็อกแต่ละ Inventory ให้เรียบร้อย
                if ("cancel".equalsIgnoreCase(action)) {
                        for (OrderItem item : order.getItems()) {
                                if (item.getInventory() != null) {
                                        cardInventoryRepository.save(item.getInventory());
                                }
                        }
                }

                // 4. บันทึก Entity ที่อัปเดตสถานะใหม่ลงฐานข้อมูล
                Order savedOrder = orderRepository.save(order);
                log.info("Order id: {} successfully transitioned to status: {}", id, savedOrder.getOrderStatus());

                return OrderResponse.fromEntity(savedOrder);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponse getOrderById(Long id) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
                        if (!isStaffOrAdmin) {
                                if (order.getUser() != null && !order.getUser().getUsername().equals(auth.getName())) {
                                        throw new AccessDeniedException("Access denied: You do not have permission to view this order");
                                }
                        }
                }

                return OrderResponse.fromEntity(order);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderResponse> getAllOrders() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                        boolean isStaffOrAdmin = auth.getAuthorities().stream()
                                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
                        if (!isStaffOrAdmin) {
                                User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
                                if (currentUser != null) {
                                        return orderRepository.findByUserId(currentUser.getId()).stream()
                                                        .map(OrderResponse::fromEntity)
                                                        .toList();
                                }
                        }
                }

                return orderRepository.findAll().stream()
                                .map(OrderResponse::fromEntity)
                                .toList();
        }

        @Override
        public OrderItemResponse updateItemTradeStatus(Long orderId, Long itemId, TradeFulfillmentStatus status) {
                // ตรวจสอบความถูกต้องของสถานะเป้าหมาย (รองรับเฉพาะ TRADE_SENT และ COMPLETED)
                if (status == null || (status != TradeFulfillmentStatus.TRADE_SENT && status != TradeFulfillmentStatus.COMPLETED)) {
                        throw new IllegalArgumentException("Unsupported trade status: " + status
                                        + ". Only TRADE_SENT and COMPLETED are allowed.");
                }

                // 1. ค้นหา Order
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

                // 2. ค้นหา OrderItem ภายใน Order นั้น (ป้องกัน IDOR / เข้าถึงไอเทมข้ามออเดอร์)
                OrderItem targetItem = order.getItems().stream()
                                .filter(item -> item.getId() != null && item.getId().equals(itemId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", itemId));

                // 3. Idempotency Check: หากสถานะตรงกับปัจจุบันอยู่แล้ว ให้คืนค่าทันทีโดยไม่ประมวลผลซ้ำ
                if (targetItem.getTradeStatus() == status) {
                        log.info("OrderItem id: {} in order id: {} is already in status: {}. Returning current state.",
                                        itemId, orderId, status);
                        return OrderItemResponse.fromEntity(targetItem);
                }

                // 4. ตรวจสอบว่าคำสั่งซื้อยังไม่ถูกยกเลิกหรือจบไปแล้ว
                if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
                        throw new TradeStateConflictException(
                                        "Cannot modify trade status for an order in terminal state: " + order.getOrderStatus());
                }

                // 5. ตรวจสอบเงื่อนไขตามลำดับ Invariant: UNASSIGNED -> FRIEND_PENDING -> TRADE_SENT -> COMPLETED
                if (status == TradeFulfillmentStatus.TRADE_SENT) {
                        // ต้องมีบัญชีเกมที่จับคู่แล้ว
                        if (targetItem.getAssignedAccount() == null) {
                                throw new TradeStateConflictException(
                                                "Cannot set TRADE_SENT: Game account has not been assigned to OrderItem id: " + itemId);
                        }
                        // ออเดอร์ต้องอยู่ในสถานะ SHIPPING
                        if (order.getOrderStatus() != OrderStatus.SHIPPING) {
                                throw new TradeStateConflictException(
                                                "Cannot set TRADE_SENT: Order must be in SHIPPING status (current status: " + order.getOrderStatus() + ")");
                        }
                        // สถานะปัจจุบันต้องเป็น FRIEND_PENDING (ห้ามข้ามขั้นจาก UNASSIGNED หรือย้อนจาก COMPLETED)
                        if (targetItem.getTradeStatus() != TradeFulfillmentStatus.FRIEND_PENDING) {
                                throw new TradeStateConflictException(
                                                "Cannot transition to TRADE_SENT from current status: " + targetItem.getTradeStatus()
                                                                + ". Expected: FRIEND_PENDING");
                        }
                } else if (status == TradeFulfillmentStatus.COMPLETED) {
                        // เปลี่ยนเป็น COMPLETED ได้จาก TRADE_SENT เท่านั้น
                        if (targetItem.getTradeStatus() != TradeFulfillmentStatus.TRADE_SENT) {
                                throw new TradeStateConflictException(
                                                "Cannot transition to COMPLETED from current status: " + targetItem.getTradeStatus()
                                                                + ". Expected: TRADE_SENT");
                        }
                }

                // 6. อัปเดตสถานะการเทรดของไอเทม
                targetItem.setTradeStatus(status);
                log.info("Updated trade status for order item [{}] in order [{}] to {}",
                                itemId, order.getOrderCode(), status);

                // 7. Auto-sync: เมื่อทุกรายการในออเดอร์เป็น COMPLETED ให้เปลี่ยนออเดอร์เป็น COMPLETED ผ่าน GoF State Pattern
                boolean allItemsCompleted = order.getItems().stream()
                                .allMatch(item -> item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED);

                if (allItemsCompleted && !order.getItems().isEmpty()) {
                        log.info("All trade items completed for order [{}] (id: {}). Transitioning order to COMPLETED via State Pattern.",
                                        order.getOrderCode(), order.getId());
                        OrderContext context = OrderContext.fromOrder(order);
                        context.complete();
                }

                orderRepository.save(order);
                return OrderItemResponse.fromEntity(targetItem);
        }

        @Override
        public OrderItemResponse reassignOrderItemAccount(Long orderId, Long orderItemId, Long newAccountId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

                if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
                        throw new TradeStateConflictException(
                                        "Cannot reassign account for an order in terminal state: " + order.getOrderStatus());
                }

                OrderItem targetItem = order.getItems().stream()
                                .filter(item -> item.getId() != null && item.getId().equals(orderItemId))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

                if (targetItem.getTradeStatus() == TradeFulfillmentStatus.TRADE_SENT ||
                    targetItem.getTradeStatus() == TradeFulfillmentStatus.COMPLETED) {
                        throw new TradeStateConflictException(
                                        "Cannot reassign account for OrderItem in status: " + targetItem.getTradeStatus());
                }

                GameAccount newAccount = gameAccountRepository.findById(newAccountId)
                                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", newAccountId));

                if (newAccount.getTradeStatus() != AccountTradeStatus.READY) {
                        throw new TradeStateConflictException(
                                        "Cannot assign account " + newAccount.getAccountCode() + " because its status is: " + newAccount.getTradeStatus());
                }

                CardInventory oldInv = targetItem.getInventory();
                if (oldInv != null && oldInv.getGameAccount() != null && oldInv.getGameAccount().getId().equals(newAccountId)) {
                        targetItem.setAssignedAccount(newAccount);
                        targetItem.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
                } else {
                        Card card = (oldInv != null) ? oldInv.getCard() : null;
                        if (card == null) {
                                throw new IllegalStateException("Card information not found for OrderItem id: " + orderItemId);
                        }
                        CardCondition requiredCondition = (oldInv != null) ? oldInv.getCondition() : null;
                        List<CardInventory> targetInventories = cardInventoryRepository.findByCardId(card.getId());
                        CardInventory newInv = targetInventories.stream()
                                        .filter(inv -> inv.getGameAccount() != null && inv.getGameAccount().getId().equals(newAccountId))
                                        .filter(inv -> requiredCondition == null || inv.getCondition() == requiredCondition)
                                        .findFirst()
                                        .orElseThrow(() -> new InsufficientStockException(
                                                        "Account " + newAccount.getAccountCode() + " does not hold card " + card.getName()
                                                                        + (requiredCondition != null ? " with condition " + requiredCondition : "")));

                        if (!newInv.hasSufficientStock(targetItem.getQuantity())) {
                                throw new InsufficientStockException(
                                                "Account " + newAccount.getAccountCode() + " has insufficient stock for: " + card.getName()
                                                                + (requiredCondition != null ? " (" + requiredCondition + ")" : "")
                                                                + " (Available: " + newInv.getQuantity() + ", Requested: " + targetItem.getQuantity() + ")");
                        }

                        // ย้ายการจองสต็อกจริง: คืนคลังเก่า หักคลังใหม่
                        if (oldInv != null) {
                                oldInv.restoreStock(targetItem.getQuantity());
                                cardInventoryRepository.save(oldInv);
                        }
                        newInv.deductStock(targetItem.getQuantity());
                        cardInventoryRepository.save(newInv);

                        targetItem.setInventory(newInv);
                        targetItem.setAssignedAccount(newAccount);
                        targetItem.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
                }

                orderRepository.save(order);
                log.info("Reassigned OrderItem [{}] to GameAccount [{}] ({}) and transferred stock cleanly",
                                orderItemId, newAccount.getAccountCode(), newAccount.getInGameName());

                return OrderItemResponse.fromEntity(targetItem);
        }

        private String generateOrderCode() {
                long count = orderRepository.count() + 1;
                return String.format("ORD-%d-%03d", LocalDate.now().getYear(), count);
        }
}
