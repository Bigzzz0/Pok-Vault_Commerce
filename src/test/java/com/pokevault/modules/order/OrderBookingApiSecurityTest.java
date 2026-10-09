package com.pokevault.modules.order;

import com.pokevault.common.security.CustomUserDetailsService;
import com.pokevault.common.security.SecurityConfig;
import com.pokevault.common.security.OrderAccessPolicy;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ทดสอบการทำงานร่วมกับ SecurityConfig จริง:
 * Guest ต้องล็อกอินก่อนจอง; STAFF จองแทนลูกค้าได้โดยใช้ session และ REST CSRF configuration เดิม
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

    @MockitoBean(name = "orderAccessPolicy")
    private OrderAccessPolicy orderAccessPolicy;

    @Test
    @DisplayName("POST /api/v1/orders: Guest ต้องล็อกอินก่อน (401)")
    void placeOrder_Anonymous_Returns401() throws Exception {

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
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(orderService, orderAccessPolicy);
    }

    @Test
    @DisplayName("POST /api/v1/orders: STAFF จองแทนได้โดยไม่ต้องส่ง CSRF token")
    void placeOrder_StaffWithoutCsrf_Returns201() throws Exception {
        when(orderAccessPolicy.canAccessCustomer(any(), any())).thenReturn(true);
        when(orderService.createOrder(any())).thenReturn(OrderResponse.builder().id(1L).userId(1L).orderCode("ORD-2026-001").build());
        mockMvc.perform(post("/api/v1/orders").with(user("staff").roles("STAFF"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"userId":1,"customerFriendId":"1234-5678-9012-3456",
                     "items":[{"inventoryId":100,"quantity":1}]}
                    """))
                .andExpect(status().isCreated());
    }
}
