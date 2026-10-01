package com.pokevault.modules.order.service;

import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(PlaceOrderRequest request);

    OrderResponse transitionOrderStatus(Long id, String action);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getAllOrders();
}
