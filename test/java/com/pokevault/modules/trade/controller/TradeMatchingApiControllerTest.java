package com.pokevault.modules.trade.controller;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.trade.advice.GlobalExceptionHandler;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.modules.trade.service.TradeMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeMatchingApiController: REST API & Exception Mapping Unit Tests")
class TradeMatchingApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TradeMatchingService tradeMatchingService;

    private TradeRecommendationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        TradeMatchingApiController controller = new TradeMatchingApiController(tradeMatchingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = TradeRecommendationResponse.builder()
                .orderId(101L)
                .orderCode("ORD-2026-TEST")
                .orderItemId(501L)
                .cardId(1L)
                .cardName("Charizard ex")
                .cardNumber("006/165")
                .requestedQuantity(1)
                .fulfillmentStatus(TradeFulfillmentStatus.UNASSIGNED)
                .matchFound(true)
                .recommendedAccountId(10L)
                .recommendedAccountCode("ACC-READY-01")
                .recommendedInGameName("Red_Trader")
                .recommendedFriendId("1111-2222-3333")
                .accountStatus(AccountTradeStatus.READY)
                .availableStock(5)
                .recommendationReason("Recommended: Account ACC-READY-01 has 5 copies in stock.")
                .alternativeCandidates(List.of(
                        TradeRecommendationResponse.CandidateAccountResponse.builder()
                                .accountId(20L)
                                .accountCode("ACC-READY-02")
                                .inGameName("Blue_Trader")
                                .friendId("4444-5555-6666")
                                .tradeStatus(AccountTradeStatus.READY)
                                .availableStock(2)
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("200 OK: GET /orders/{orderId}/recommendations ดึงรายการแนะนำไอดีเกมสำหรับทั้งคำสั่งซื้อสำเร็จ")
    void getOrderRecommendations_Success_Returns200() throws Exception {
        when(tradeMatchingService.getRecommendations(101L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/trades/orders/101/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Trade recommendations retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderId").value(101))
                .andExpect(jsonPath("$.data[0].cardName").value("Charizard ex"))
                .andExpect(jsonPath("$.data[0].matchFound").value(true))
                .andExpect(jsonPath("$.data[0].recommendedAccountCode").value("ACC-READY-01"))
                .andExpect(jsonPath("$.data[0].alternativeCandidates[0].accountCode").value("ACC-READY-02"));
    }

    @Test
    @DisplayName("404 Not Found: GET /orders/{orderId}/recommendations เมื่อไม่พบ Order")
    void getOrderRecommendations_NotFound_Returns404() throws Exception {
        when(tradeMatchingService.getRecommendations(999L))
                .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(get("/api/v1/trades/orders/999/recommendations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order not found with id : '999'"));
    }

    @Test
    @DisplayName("200 OK: GET /items/{orderItemId}/recommendation ดึงคำแนะนำสำหรับรายการสินค้าเดี่ยวสำเร็จ")
    void getItemRecommendation_Success_Returns200() throws Exception {
        when(tradeMatchingService.getRecommendationForItem(501L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/trades/items/501/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Trade recommendation retrieved successfully"))
                .andExpect(jsonPath("$.data.orderItemId").value(501))
                .andExpect(jsonPath("$.data.cardName").value("Charizard ex"))
                .andExpect(jsonPath("$.data.recommendedAccountCode").value("ACC-READY-01"));
    }

    @Test
    @DisplayName("404 Not Found: GET /items/{orderItemId}/recommendation เมื่อไม่พบ OrderItem")
    void getItemRecommendation_NotFound_Returns404() throws Exception {
        when(tradeMatchingService.getRecommendationForItem(999L))
                .thenThrow(new ResourceNotFoundException("OrderItem", "id", 999L));

        mockMvc.perform(get("/api/v1/trades/items/999/recommendation"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("200 OK: POST /items/{orderItemId}/auto-match จับคู่ไอดีเกมอัตโนมัติสำเร็จ")
    void autoMatchOrderItem_Success_Returns200() throws Exception {
        sampleResponse.setFulfillmentStatus(TradeFulfillmentStatus.FRIEND_PENDING);
        sampleResponse.setCurrentAssignedAccountId(10L);

        when(tradeMatchingService.autoMatchOrderItem(501L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/trades/items/501/auto-match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account auto-matched successfully to order item"))
                .andExpect(jsonPath("$.data.matchFound").value(true))
                .andExpect(jsonPath("$.data.fulfillmentStatus").value("FRIEND_PENDING"))
                .andExpect(jsonPath("$.data.currentAssignedAccountId").value(10));
    }

    @Test
    @DisplayName("400 Bad Request: POST /items/{orderItemId}/auto-match สต็อกไม่พอ (InsufficientStockException)")
    void autoMatchOrderItem_InsufficientStock_Returns400() throws Exception {
        when(tradeMatchingService.autoMatchOrderItem(501L))
                .thenThrow(new InsufficientStockException("No READY game accounts have stock for card: Charizard ex"));

        mockMvc.perform(post("/api/v1/trades/items/501/auto-match"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value("No READY game accounts have stock for card: Charizard ex"));
    }

    @Test
    @DisplayName("409 Conflict: POST /items/{orderItemId}/auto-match สินค้าเทรดเสร็จแล้ว (InvalidOrderStateException)")
    void autoMatchOrderItem_AlreadyCompleted_Returns409() throws Exception {
        when(tradeMatchingService.autoMatchOrderItem(501L))
                .thenThrow(new InvalidOrderStateException("Cannot reassign account for OrderItem in status: COMPLETED"));

        mockMvc.perform(post("/api/v1/trades/items/501/auto-match"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("STATE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("Cannot reassign account for OrderItem in status: COMPLETED"));
    }

    @Test
    @DisplayName("200 OK: POST /orders/{orderId}/auto-match จับคู่ไอดีเกมอัตโนมัติสำหรับทั้งคำสั่งซื้อสำเร็จ")
    void autoMatchOrder_Success_Returns200() throws Exception {
        when(tradeMatchingService.autoMatchOrder(101L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(post("/api/v1/trades/orders/101/auto-match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Accounts auto-matched successfully for all order items"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].orderItemId").value(501));
    }

    @Test
    @DisplayName("404 Not Found: POST /orders/{orderId}/auto-match เมื่อไม่พบ Order")
    void autoMatchOrder_NotFound_Returns404() throws Exception {
        when(tradeMatchingService.autoMatchOrder(999L))
                .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(post("/api/v1/trades/orders/999/auto-match"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("200 OK: POST /items/{orderItemId}/assign มอบหมายไอดีเกมด้วยตนเองสำเร็จ")
    void assignAccountToOrderItem_Success_Returns200() throws Exception {
        sampleResponse.setCurrentAssignedAccountId(20L);
        sampleResponse.setFulfillmentStatus(TradeFulfillmentStatus.FRIEND_PENDING);

        when(tradeMatchingService.assignAccountToOrderItem(501L, 20L)).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/trades/items/501/assign")
                        .param("accountId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account assigned successfully to order item"))
                .andExpect(jsonPath("$.data.currentAssignedAccountId").value(20));
    }

    @Test
    @DisplayName("409 Conflict: POST /items/{orderItemId}/assign เมื่อไอดีเกมไม่ได้อยู่ในสถานะ READY")
    void assignAccountToOrderItem_AccountNotReady_Returns409() throws Exception {
        when(tradeMatchingService.assignAccountToOrderItem(501L, 30L))
                .thenThrow(new InvalidOrderStateException("Cannot assign account ACC-BUSY-01 because its status is: BUSY_TRADING"));

        mockMvc.perform(post("/api/v1/trades/items/501/assign")
                        .param("accountId", "30"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("STATE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("Cannot assign account ACC-BUSY-01 because its status is: BUSY_TRADING"));
    }

    @Test
    @DisplayName("404 Not Found: POST /items/{orderItemId}/assign เมื่อไม่พบไอดีเกม (ResourceNotFoundException)")
    void assignAccountToOrderItem_AccountNotFound_Returns404() throws Exception {
        when(tradeMatchingService.assignAccountToOrderItem(501L, 888L))
                .thenThrow(new ResourceNotFoundException("GameAccount", "id", 888L));

        mockMvc.perform(post("/api/v1/trades/items/501/assign")
                        .param("accountId", "888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("400 Bad Request: POST /items/{orderItemId}/assign เมื่อไม่ได้ระบุ parameter accountId")
    void assignAccountToOrderItem_MissingAccountId_Returns400() throws Exception {
        mockMvc.perform(post("/api/v1/trades/items/501/assign"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'accountId' is missing"));
    }
}
