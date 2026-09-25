package com.pokevault.modules.order.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Endpoints for booking cards, order engine, and order management")
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place card booking order", description = "Book cards with stock deduction and automatic membership discount calculation")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve single order details by primary key ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok("Order retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve list of all customer orders in the system")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + orders.size() + " orders successfully", orders));
    }
}
