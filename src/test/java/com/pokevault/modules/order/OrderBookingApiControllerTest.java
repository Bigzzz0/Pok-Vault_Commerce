package com.pokevault.modules.order;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.modules.order.controller.OrderApiController;
import com.pokevault.modules.order.dto.OrderItemResponse;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.service.OrderService;
import com.pokevault.modules.trade.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderBookingApiControllerTest: Member 3 Booking REST API & Validation")
class OrderBookingApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private static final String VALID_PAYLOAD = """
        {
          "userId": 1,
          "customerFriendId": "1234-5678-9012-3456",
          "customerInGameName": "AshKetchum",
          "items": [
            {
              "inventoryId": 100,
              "quantity": 2
            }
          ],
          "notes": "Trade after 8 PM"
        }
        """;

    @BeforeEach
    void setUp() {
        OrderApiController controller = new OrderApiController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("201 CREATED: POST /api/v1/orders สำเร็จเมื่อส่งข้อมูลครบถ้วนถูกต้อง")
    void createOrder_Success_Returns201() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-2026-001")
                .orderStatus(OrderStatus.PENDING)
                .customerFriendId("1234-5678-9012-3456")
                .customerInGameName("AshKetchum")
                .totalAmount(new BigDecimal("1980.00"))
                .discountAmount(new BigDecimal("198.00"))
                .finalAmount(new BigDecimal("1782.00"))
                .createdAt(LocalDateTime.now())
                .items(List.of(
                        OrderItemResponse.builder()
                                .id(10L)
                                .cardName("Pikachu ex")
                                .quantity(2)
                                .unitPrice(new BigDecimal("990.00"))
                                .subtotal(new BigDecimal("1980.00"))
                                .build()
                ))
                .build();

        when(orderService.createOrder(any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-2026-001"))
                .andExpect(jsonPath("$.data.orderStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.finalAmount").value(1782.00));

        verify(orderService, times(1)).createOrder(any(PlaceOrderRequest.class));
    }

    @Test
    @DisplayName("201 CREATED: รองรับรหัสเพื่อน 16 หลักแบบไม่มีขีดคั่น (16 continuous digits)")
    void createOrder_ContinuousDigitsFriendId_Returns201() throws Exception {
        String payload = """
            {
              "userId": 1,
              "customerFriendId": "1234567890123456",
              "items": [{"inventoryId": 100, "quantity": 1}]
            }
            """;

        OrderResponse response = OrderResponse.builder()
                .id(2L)
                .orderCode("ORD-2026-002")
                .orderStatus(OrderStatus.PENDING)
                .customerFriendId("1234567890123456")
                .build();

        when(orderService.createOrder(any(PlaceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customerFriendId").value("1234567890123456"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1234",
        "1234-5678-9012",
        "abcd-efgh-ijkl-mnop",
        "123456789012345",
        "12345678901234567",
        "1234_5678_9012_3456"
    })
    @DisplayName("400 BAD_REQUEST: รหัสเพื่อนในเกมไม่ถูกต้องตาม Pattern Regex 16 หลัก")
    void createOrder_InvalidFriendCode_Returns400(String invalidCode) throws Exception {
        String payload = String.format("""
            {
              "userId": 1,
              "customerFriendId": "%s",
              "items": [{"inventoryId": 100, "quantity": 1}]
            }
            """, invalidCode);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.customerFriendId").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: ไม่ระบุ userId (userId is null)")
    void createOrder_MissingUserId_Returns400() throws Exception {
        String payload = """
            {
              "customerFriendId": "1234-5678-9012-3456",
              "items": [{"inventoryId": 100, "quantity": 1}]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.userId").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: รายการสินค้าว่างเปล่า (items is empty)")
    void createOrder_EmptyItemsList_Returns400() throws Exception {
        String payload = """
            {
              "userId": 1,
              "customerFriendId": "1234-5678-9012-3456",
              "items": []
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.items").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: จำนวนสินค้าเป็น 0 หรือติดลบ (quantity < 1)")
    void createOrder_InvalidItemQuantity_Returns400() throws Exception {
        String payload = """
            {
              "userId": 1,
              "customerFriendId": "1234-5678-9012-3456",
              "items": [{"inventoryId": 100, "quantity": 0}]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details['items[0].quantity']").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: ไม่ระบุ inventoryId ในรายการสินค้า")
    void createOrder_MissingInventoryId_Returns400() throws Exception {
        String payload = """
            {
              "userId": 1,
              "customerFriendId": "1234-5678-9012-3456",
              "items": [{"quantity": 1}]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details['items[0].inventoryId']").exists());

        verify(orderService, never()).createOrder(any());
    }

    @Test
    @DisplayName("400 BAD_REQUEST: สต็อกการ์ดไม่เพียงพอ (InsufficientStockException)")
    void createOrder_InsufficientStock_Returns400() throws Exception {
        when(orderService.createOrder(any(PlaceOrderRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for card: Mewtwo ex (Available: 1, Requested: 2)"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message").value("Insufficient stock for card: Mewtwo ex (Available: 1, Requested: 2)"));
    }

    @Test
    @DisplayName("200 OK: GET /api/v1/orders/{id} สำเร็จเมื่อพบข้อมูลออเดอร์")
    void getOrderById_Success_Returns200() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-2026-001")
                .orderStatus(OrderStatus.PENDING)
                .finalAmount(new BigDecimal("891.00"))
                .build();

        when(orderService.getOrderById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-2026-001"));
    }

    @Test
    @DisplayName("404 NOT_FOUND: GET /api/v1/orders/{id} เมื่อไม่พบออเดอร์ในระบบ")
    void getOrderById_NotFound_Returns404() throws Exception {
        when(orderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order", "id", 999L));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("200 OK: GET /api/v1/orders ดึงรายการออเดอร์ทั้งหมดสำเร็จ")
    void getAllOrders_Success_Returns200() throws Exception {
        OrderResponse order1 = OrderResponse.builder().id(1L).orderCode("ORD-2026-001").build();
        OrderResponse order2 = OrderResponse.builder().id(2L).orderCode("ORD-2026-002").build();

        when(orderService.getAllOrders()).thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderCode").value("ORD-2026-001"))
                .andExpect(jsonPath("$.data[1].orderCode").value("ORD-2026-002"));
    }
}
