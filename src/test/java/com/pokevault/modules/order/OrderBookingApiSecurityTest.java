package com.pokevault.modules.order;

import com.pokevault.common.security.CustomUserDetailsService;
import com.pokevault.common.security.SecurityConfig;
import com.pokevault.modules.order.controller.OrderApiController;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ทดสอบการทำงานร่วมกับ SecurityConfig จริง:
 * ยืนยันว่า endpoint จองการ์ด POST /api/v1/orders เป็น public (permitAll) และไม่ต้องใช้ CSRF token
 * ผู้รับผิดชอบ: สมาชิกคนที่ 3 (Order Engine & Strategy Pattern Specialist)
 */
@WebMvcTest(OrderApiController.class)
@Import(SecurityConfig.class)
@DisplayName("OrderBookingApiSecurityTest: SecurityConfig & CSRF integration for Order Booking API")
class OrderBookingApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders: Guest เรียกได้โดยไม่ต้องล็อกอิน และไม่ต้องส่ง CSRF token (permitAll + csrf ignored)")
    void placeOrder_PublicWithoutCsrf_Returns201() throws Exception {
        when(orderService.createOrder(any())).thenReturn(OrderResponse.builder().id(1L).orderCode("ORD-2026-001").build());

        String payload = """
            {
              "userId": 1,
              "customerFriendId": "1234-5678-9012-3456",
              "items": [{"inventoryId": 100, "quantity": 1}]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }
}
