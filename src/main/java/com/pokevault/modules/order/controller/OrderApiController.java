package com.pokevault.modules.order.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Endpoints for booking cards, order engine, and State Pattern lifecycle")
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place card booking order", description = "Book cards with stock deduction and automatic membership discount calculation")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition order state", description = "Execute state transition action on order lifecycle using GoF State Pattern. "
            + "Valid actions: pay, ship, complete, cancel.")
    public ResponseEntity<ApiResponse<OrderResponse>> transitionOrderStatus(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Action to execute (pay, ship, complete, cancel)", example = "pay") @RequestParam String action) {

        OrderResponse response = orderService.transitionOrderStatus(id, action);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully to " + response.getOrderStatus(), response));
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

    @PatchMapping("/{orderId}/items/{orderItemId}/trade-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Update order item trade status",
            description = "Update in-game trade fulfillment status for a specific card item (TRADE_SENT or COMPLETED). "
                    + "Enforces strict lifecycle sequence: UNASSIGNED -> FRIEND_PENDING -> TRADE_SENT -> COMPLETED. "
                    + "TRADE_SENT requires an assigned game account and order in SHIPPING status. "
                    + "Automatically transitions order to COMPLETED via GoF State Pattern when all items are COMPLETED. "
                    + "Idempotent: repeating the current status returns current state without duplicate action. "
                    + "Restricted to ADMIN and STAFF roles only (CUSTOMER will receive 403 Forbidden)."
    )
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateItemTradeStatus(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long orderId,
            @Parameter(description = "Order Item ID", example = "1") @PathVariable Long orderItemId,
            @Parameter(description = "Target trade status (TRADE_SENT or COMPLETED)", example = "TRADE_SENT") @RequestParam TradeFulfillmentStatus status) {

        OrderItemResponse response = orderService.updateItemTradeStatus(orderId, orderItemId, status);
        return ResponseEntity.ok(ApiResponse.ok("Order item trade status updated successfully", response));
    }

    @PatchMapping("/{orderId}/items/{orderItemId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(
            summary = "Reassign game account for order item",
            description = "Reassigns trade account for an order item and transfers reserved card stock between inventories. "
                    + "Restricted to ADMIN and STAFF roles."
    )
    public ResponseEntity<ApiResponse<OrderItemResponse>> reassignOrderItemAccount(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long orderId,
            @Parameter(description = "Order Item ID", example = "1") @PathVariable Long orderItemId,
            @Parameter(description = "Target Game Account ID", example = "2") @RequestParam Long accountId) {

        OrderItemResponse response = orderService.reassignOrderItemAccount(orderId, orderItemId, accountId);
        return ResponseEntity.ok(ApiResponse.ok("Order item account reassigned successfully", response));
    }
}
