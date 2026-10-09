package com.pokevault.common.security;

import com.pokevault.domain.entity.User;
import com.pokevault.modules.catalog.service.CardService;
import com.pokevault.modules.order.dto.OrderResponse;
import com.pokevault.modules.order.dto.PlaceOrderRequest;
import com.pokevault.modules.order.service.OrderService;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real security filter chain and method authorization, with mocked business operations. */
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:security-tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never", "spring.jpa.show-sql=false"})
@ActiveProfiles("security-test")
@AutoConfigureMockMvc
@Transactional
class ApiAuthorizationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @MockitoBean OrderService orders;
    @MockitoBean CardService cards;
    User red;
    User blue;

    @BeforeEach void setUp() {
        red = users.saveAndFlush(User.builder().username("security_red").email("security-red@example.test")
                .password(encoder.encode("test-password")).build());
        blue = users.saveAndFlush(User.builder().username("security_blue").email("security-blue@example.test")
                .password("synthetic-hash").build());
    }

    private String booking(Long userId) {
        return """
            {"userId":%d,"customerFriendId":"1111-2222-3333-4444",
             "items":[{"inventoryId":7,"quantity":1}]}
            """.formatted(userId);
    }

    @Test void catalogAndRegistrationRemainPublic() throws Exception {
        when(cards.getAllCards(any())).thenReturn(List.of());
        mvc.perform(get("/api/v1/cards")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @ParameterizedTest @ValueSource(strings = {"GET /api/v1/accounts", "POST /api/v1/accounts",
            "POST /api/v1/accounts/1/pulls", "PATCH /api/v1/accounts/1/trade-status?status=READY",
            "GET /api/v1/orders", "GET /api/v1/orders/1", "POST /api/v1/orders",
            "PATCH /api/v1/orders/1/status?action=pay",
            "PATCH /api/v1/orders/1/items/1/trade-status?status=TRADE_SENT",
            "POST /api/v1/cards", "PUT /api/v1/cards/1", "DELETE /api/v1/cards/1",
            "GET /api/v1/trades/orders/1/recommendations", "POST /api/v1/trades/orders/1/auto-match",
            "PATCH /api/v1/admin/inventories/1/price?price=100"})
    void anonymousApiRequestsReceiveJson401(String route) throws Exception {
        String[] parts = route.split(" ", 2);
        mvc.perform(request(HttpMethod.valueOf(parts[0]), parts[1]))
                .andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
        verifyNoInteractions(orders, cards);
    }

    @ParameterizedTest @ValueSource(strings = {"GET /api/v1/accounts", "POST /api/v1/accounts",
            "POST /api/v1/accounts/1/pulls", "PATCH /api/v1/accounts/1/trade-status?status=READY",
            "GET /api/v1/orders", "PATCH /api/v1/orders/1/status?action=pay",
            "PATCH /api/v1/orders/1/items/1/trade-status?status=TRADE_SENT",
            "POST /api/v1/cards", "PUT /api/v1/cards/1", "DELETE /api/v1/cards/1",
            "GET /api/v1/trades/orders/1/recommendations", "POST /api/v1/trades/orders/1/auto-match",
            "PATCH /api/v1/admin/inventories/1/price?price=100"})
    void customerCannotUseStoreManagementApis(String route) throws Exception {
        String[] parts = route.split(" ", 2);
        mvc.perform(request(HttpMethod.valueOf(parts[0]), parts[1]).with(user(red.getUsername()).roles("CUSTOMER")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
        verifyNoInteractions(orders, cards);
    }

    @Test void customerCanBookForSelf() throws Exception {
        when(orders.createOrder(any())).thenReturn(OrderResponse.builder().id(10L).userId(red.getId()).build());
        mvc.perform(post("/api/v1/orders").with(user(red.getUsername()).roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(booking(red.getId())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.userId").value(red.getId().intValue()));
        var capture = ArgumentCaptor.forClass(PlaceOrderRequest.class);
        verify(orders).createOrder(capture.capture());
        assertThat(capture.getValue().getUserId()).isEqualTo(red.getId());
    }

    @Test void customerCannotSpoofAnotherCustomerId() throws Exception {
        mvc.perform(post("/api/v1/orders").with(user(red.getUsername()).roles("CUSTOMER"))
                .contentType(MediaType.APPLICATION_JSON).content(booking(blue.getId())))
                .andExpect(status().isForbidden());
        verifyNoInteractions(orders);
    }

    @Test void customerCanReadOwnOrder() throws Exception {
        when(orders.getOrderById(10L)).thenReturn(OrderResponse.builder().id(10L).userId(red.getId()).build());
        mvc.perform(get("/api/v1/orders/10").with(user(red.getUsername()).roles("CUSTOMER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(10));
    }

    @Test void customerCannotReadOtherCustomersOrderOrReceiveItsData() throws Exception {
        when(orders.getOrderById(11L)).thenReturn(OrderResponse.builder().id(11L).userId(blue.getId())
                .customerFriendId("sensitive-friend-id").build());
        mvc.perform(get("/api/v1/orders/11").with(user(red.getUsername()).roles("CUSTOMER")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sensitive-friend-id"))));
    }

    @ParameterizedTest @ValueSource(strings = {"STAFF", "ADMIN"})
    void staffAndAdminCanBookForCustomersReadOrdersAndManageCards(String role) throws Exception {
        when(orders.createOrder(any())).thenReturn(OrderResponse.builder().id(12L).userId(blue.getId()).build());
        when(orders.getOrderById(12L)).thenReturn(OrderResponse.builder().id(12L).userId(blue.getId()).build());
        when(orders.getAllOrders()).thenReturn(List.of());
        mvc.perform(post("/api/v1/orders").with(user("store-user").roles(role))
                .contentType(MediaType.APPLICATION_JSON).content(booking(blue.getId()))).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/orders/12").with(user("store-user").roles(role))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/orders").with(user("store-user").roles(role))).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/cards/7").with(user("store-user").roles(role))).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/accounts").with(user("store-user").roles(role))).andExpect(status().isOk());
    }

    @Test void browserStillRedirectsToLoginAndFormLoginUsesBcrypt() throws Exception {
        mvc.perform(get("/my-orders")).andExpect(status().is3xxRedirection());
        var result = mvc.perform(post("/login").with(csrf()).param("username", red.getUsername())
                .param("password", "test-password")).andExpect(status().is3xxRedirection()).andReturn();
        when(orders.getOrderById(10L)).thenReturn(OrderResponse.builder().id(10L).userId(red.getId()).build());
        mvc.perform(get("/api/v1/orders/10").session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk());
    }
}
