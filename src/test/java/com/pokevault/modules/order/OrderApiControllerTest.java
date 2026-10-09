package com.pokevault.modules.order;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.order.controller.OrderApiController;
import com.pokevault.modules.order.dto.OrderItemResponse;
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
}
