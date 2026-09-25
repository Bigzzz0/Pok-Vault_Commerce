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
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CardInventoryRepository cardInventoryRepository;
    private final UserRepository userRepository;
    private final DiscountService discountService;

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
                    .orElseThrow(() -> new ResourceNotFoundException("CardInventory", "id", itemReq.getInventoryId()));

            if (!inventory.hasSufficientStock(itemReq.getQuantity())) {
                String cardName = inventory.getCard() != null ? inventory.getCard().getName() : "ID " + inventory.getId();
                throw new InsufficientStockException("Insufficient stock for card: " + cardName
                        + " (Available: " + inventory.getQuantity() + ", Requested: " + itemReq.getQuantity() + ")");
            }

            // Deduct stock immediately
            inventory.deductStock(itemReq.getQuantity());
            cardInventoryRepository.save(inventory);

            BigDecimal unitPrice = inventory.getSellingPrice() != null ? inventory.getSellingPrice() : BigDecimal.ZERO;
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())).setScale(2, RoundingMode.HALF_UP);
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
        BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        order.setOrderCode(generateOrderCode());
        order.setTotalAmount(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);

        Order savedOrder = orderRepository.save(order);

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

    private String generateOrderCode() {
        long count = orderRepository.count() + 1;
        return String.format("ORD-%d-%03d", LocalDate.now().getYear(), count);
    }
}
