package com.pokevault.modules.order.service;

import com.pokevault.modules.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse transitionOrderStatus(Long id, String action);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();
}
