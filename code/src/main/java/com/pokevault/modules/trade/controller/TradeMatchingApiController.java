package com.pokevault.modules.trade.controller;

import com.pokevault.common.response.ApiResponse;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.modules.trade.service.TradeMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Tag(name = "Trade Matching API", description = "Endpoints for in-game trade account recommendations and auto-match fulfillment")
public class TradeMatchingApiController {

    private final TradeMatchingService tradeMatchingService;

    @GetMapping("/orders/{orderId}/recommendations")
    @Operation(summary = "Get trade recommendations for order",
            description = "Retrieve best candidate accounts and alternative options for each card item in the specified order")
    public ResponseEntity<ApiResponse<List<TradeRecommendationResponse>>> getOrderRecommendations(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long orderId) {

        List<TradeRecommendationResponse> recommendations = tradeMatchingService.getRecommendations(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Trade recommendations retrieved successfully", recommendations));
    }

    @GetMapping("/items/{orderItemId}/recommendation")
    @Operation(summary = "Get trade recommendation for single order item",
            description = "Retrieve best candidate account and alternative options for a specific order item")
    public ResponseEntity<ApiResponse<TradeRecommendationResponse>> getItemRecommendation(
            @Parameter(description = "Order Item ID", example = "1") @PathVariable Long orderItemId) {

        TradeRecommendationResponse recommendation = tradeMatchingService.getRecommendationForItem(orderItemId);
        return ResponseEntity.ok(ApiResponse.ok("Trade recommendation retrieved successfully", recommendation));
    }

    @PostMapping("/items/{orderItemId}/auto-match")
    @Operation(summary = "Auto-match best account for order item",
            description = "Automatically select the best READY game account with sufficient stock and assign it to the order item")
    public ResponseEntity<ApiResponse<TradeRecommendationResponse>> autoMatchOrderItem(
            @Parameter(description = "Order Item ID", example = "1") @PathVariable Long orderItemId) {

        TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(orderItemId);
        return ResponseEntity.ok(ApiResponse.ok("Account auto-matched successfully to order item", response));
    }

    @PostMapping("/orders/{orderId}/auto-match")
    @Operation(summary = "Auto-match best accounts for entire order",
            description = "Automatically select and assign the best READY game accounts for all card items in the specified order")
    public ResponseEntity<ApiResponse<List<TradeRecommendationResponse>>> autoMatchOrder(
            @Parameter(description = "Order ID", example = "1") @PathVariable Long orderId) {

        List<TradeRecommendationResponse> responses = tradeMatchingService.autoMatchOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Accounts auto-matched successfully for all order items", responses));
    }

    @PostMapping("/items/{orderItemId}/assign")
    @Operation(summary = "Manually assign game account to order item",
            description = "Manually assign a specific READY game account to an order item for trade fulfillment")
    public ResponseEntity<ApiResponse<TradeRecommendationResponse>> assignAccountToOrderItem(
            @Parameter(description = "Order Item ID", example = "1") @PathVariable Long orderItemId,
            @Parameter(description = "Game Account ID", example = "1") @RequestParam Long accountId) {

        TradeRecommendationResponse response = tradeMatchingService.assignAccountToOrderItem(orderItemId, accountId);
        return ResponseEntity.ok(ApiResponse.ok("Account assigned successfully to order item", response));
    }
}
