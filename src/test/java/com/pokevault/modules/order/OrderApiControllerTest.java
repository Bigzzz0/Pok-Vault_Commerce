package com.pokevault.modules.order;

import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.controller.OrderApiController;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.service.OrderService;
import com.pokevault.modules.trade.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApiController: REST API & Exception Mapping Unit Tests")
class OrderApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        OrderApiController controller = new OrderApiController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("200 OK: เรียกอัปเดตสถานะเทรดเป็น TRADE_SENT สำเร็จ")
    void updateItemTradeStatus_Success_Returns200() throws Exception {
        OrderItemResponse responseDto = OrderItemResponse.builder()
                .id(10L)
                .tradeStatus(TradeFulfillmentStatus.TRADE_SENT)
                .build();

        when(orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "TRADE_SENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tradeStatus").value("TRADE_SENT"));
    }

    @Test
    @DisplayName("200 OK: เรียกอัปเดตสถานะเทรดเป็น COMPLETED สำเร็จ")
    void updateItemTradeStatus_Completed_Returns200() throws Exception {
        OrderItemResponse responseDto = OrderItemResponse.builder()
                .id(10L)
                .tradeStatus(TradeFulfillmentStatus.COMPLETED)
                .build();

        when(orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.COMPLETED))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tradeStatus").value("COMPLETED"));
    }

    @Test
    @DisplayName("403 Forbidden: เมื่อไม่มีสิทธิ์ (AccessDeniedException)")
    void updateItemTradeStatus_AccessDenied_Returns403() throws Exception {
        when(orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                .thenThrow(new AccessDeniedException("Only ADMIN and STAFF can update trade status"));

        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "TRADE_SENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("400 Bad Request: ส่งชื่อสถานะไม่ถูกต้อง (Type Mismatch)")
    void updateItemTradeStatus_InvalidStatus_Returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "UNKNOWN_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("400 Bad Request: ส่งสถานะที่ไม่รองรับ (IllegalArgumentException)")
    void updateItemTradeStatus_UnsupportedStatus_Returns400() throws Exception {
        when(orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.UNASSIGNED))
                .thenThrow(new IllegalArgumentException("Unsupported trade status: UNASSIGNED. Only TRADE_SENT and COMPLETED are allowed."));

        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "UNASSIGNED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("404 Not Found: ไม่พบ Order หรือ OrderItem (ResourceNotFoundException)")
    void updateItemTradeStatus_NotFound_Returns404() throws Exception {
        when(orderService.updateItemTradeStatus(999L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(patch("/api/v1/orders/999/items/10/trade-status")
                        .param("status", "TRADE_SENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("409 Conflict: ผิดลำดับขั้นตอน / ยังไม่จับคู่ (TradeStateConflictException)")
    void updateItemTradeStatus_StateConflict_Returns409() throws Exception {
        when(orderService.updateItemTradeStatus(1L, 10L, TradeFulfillmentStatus.TRADE_SENT))
                .thenThrow(new TradeStateConflictException("Order must be in SHIPPING status"));

        mockMvc.perform(patch("/api/v1/orders/1/items/10/trade-status")
                        .param("status", "TRADE_SENT"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("STATE_CONFLICT"));
    }

    // ==========================================
    // State Pattern Transition Endpoint Tests
    // ==========================================

    @Test
    @DisplayName("200 OK: PATCH /api/v1/orders/{id}/status?action=pay สำเร็จ")
    void transitionOrderStatus_Pay_Returns200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-2026-001")
                .orderStatus(OrderStatus.PAID)
                .build();

        when(orderService.transitionOrderStatus(1L, "pay")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("action", "pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"));
    }

    @Test
    @DisplayName("200 OK: PATCH /api/v1/orders/{id}/status?action=ship สำเร็จ")
    void transitionOrderStatus_Ship_Returns200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-2026-001")
                .orderStatus(OrderStatus.SHIPPING)
                .build();

        when(orderService.transitionOrderStatus(1L, "ship")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("action", "ship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("SHIPPING"));
    }

    @Test
    @DisplayName("200 OK: PATCH /api/v1/orders/{id}/status?action=cancel สำเร็จ")
    void transitionOrderStatus_Cancel_Returns200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-2026-001")
                .orderStatus(OrderStatus.CANCELLED)
                .build();

        when(orderService.transitionOrderStatus(1L, "cancel")).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("action", "cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));
    }

    @Test
    @DisplayName("409 Conflict: PATCH /api/v1/orders/{id}/status ขัดต่อกฎสถานะ (เช่น ยกเลิกระหว่าง SHIPPING)")
    void transitionOrderStatus_InvalidStateTransition_Returns409() throws Exception {
        when(orderService.transitionOrderStatus(1L, "cancel"))
                .thenThrow(new InvalidOrderStateException("Cannot cancel order while cards are being shipped in game"));

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("action", "cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("STATE_CONFLICT"))
                .andExpect(jsonPath("$.message").value("Cannot cancel order while cards are being shipped in game"));
    }

    @Test
    @DisplayName("404 Not Found: PATCH /api/v1/orders/{id}/status เมื่อไม่พบออเดอร์")
    void transitionOrderStatus_OrderNotFound_Returns404() throws Exception {
        when(orderService.transitionOrderStatus(999L, "pay"))
                .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(patch("/api/v1/orders/999/status")
                        .param("action", "pay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("400 Bad Request: PATCH /api/v1/orders/{id}/status ขาด parameter action")
    void transitionOrderStatus_MissingActionParam_Returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/status"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'action' is missing"));
    }
}
