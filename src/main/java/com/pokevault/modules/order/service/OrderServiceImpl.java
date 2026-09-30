package com.pokevault.modules.order.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Order;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.trade.state.OrderContext;
import com.pokevault.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

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
}
