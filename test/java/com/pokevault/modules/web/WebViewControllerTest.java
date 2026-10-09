package com.pokevault.modules.web;

import com.pokevault.common.security.CustomUserDetailsService;
import com.pokevault.common.security.SecurityConfig;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.modules.web.controller.WebViewController;
import com.pokevault.modules.web.dto.view.AccountsPage;
import com.pokevault.modules.web.dto.view.CardGalleryPage;
import com.pokevault.modules.web.dto.view.CardView;
import com.pokevault.modules.web.dto.view.CustomerView;
import com.pokevault.modules.web.dto.view.DashboardPage;
import com.pokevault.modules.web.dto.view.FilterOption;
import com.pokevault.modules.web.dto.view.GameAccountView;
import com.pokevault.modules.web.dto.view.InventoryPage;
import com.pokevault.modules.web.dto.view.InventoryView;
import com.pokevault.modules.web.dto.view.OrderItemView;
import com.pokevault.modules.web.dto.view.OrderView;
import com.pokevault.modules.web.service.WebPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web MVC Test สำหรับหน้าเว็บ Thymeleaf (WebViewController) + เมนู/สิทธิ์ตามบทบาทผู้ใช้
 * mock WebPageService แทน Repository: เทสต์ชั้น presentation เท่านั้น (ส่ง parameter ให้ service, ใส่ Model, render template)
 * กฎของร้านทดสอบแยกใน WebPageServiceTest
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@WebMvcTest(WebViewController.class)
@Import(SecurityConfig.class)
class WebViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebPageService webPageService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    // @EnableJpaAuditing บน PokevaultApplication ต้องการ JPA metamodel ซึ่ง @WebMvcTest ไม่ได้โหลด
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final CardView CHARIZARD = CardView.builder()
            .id(2L).name("Charizard ex").cardNumber("280/226").expansionCode("A1")
            .rarity(Rarity.CROWN_RARE).cardType(CardType.POKEMON).elementType(ElementType.FIRE).hp(180)
            .imageUrl("/images/cards/A1_280_EN.png").totalStock(2).orderInventoryId(22L).price(new BigDecimal("1500.00"))
            .build();

    private static final CustomerView RED = CustomerView.builder()
            .id(7L).username("customer_red").email("red@pokevault.local").displayName("Red")
            .membershipTier(MembershipTier.VIP).build();

    private static final OrderView ORDER = OrderView.builder()
            .id(9L).orderCode("ORD-2026-009").createdAt(LocalDateTime.of(2026, 10, 9, 14, 30))
            .username("customer_red").customerFullName("Red").customerFriendId("1234-5678-9012-3456")
            .customerInGameName("RedMaster").membershipTier(MembershipTier.VIP).orderStatus(OrderStatus.PENDING)
            .items(List.of(OrderItemView.builder().cardName("Charizard ex").imageUrl("/images/cards/A1_280_EN.png")
                    .expansionCode("A1").cardNumber("280/226").quantity(1).unitPrice(new BigDecimal("1500.00")).build()))
            .discountAmount(new BigDecimal("150.00")).finalAmount(new BigDecimal("1350.00"))
            .build();

    /** หน้าที่ไม่มีข้อมูล: ให้ทุกหน้า render ได้ในเทสต์เรื่องสิทธิ์/navbar */
    @BeforeEach
    void emptyPages() {
        when(webPageService.getDashboard()).thenReturn(DashboardPage.builder().featuredCards(List.of()).build());
        when(webPageService.getCardGallery(any(), any(), any(), any())).thenReturn(CardGalleryPage.builder()
                .cards(List.of()).elementFilters(List.of()).rarityFilters(List.of()).typeFilters(List.of())
                .allElementsUrl("/cards").allRaritiesUrl("/cards").allTypesUrl("/cards").build());
        when(webPageService.getInventoryPage()).thenReturn(InventoryPage.builder()
                .inventories(List.of()).customers(List.of()).build());
        when(webPageService.getAccountsPage()).thenReturn(AccountsPage.builder()
                .accounts(List.of()).customers(List.of()).build());
        when(webPageService.getAllOrders()).thenReturn(List.of());
        when(webPageService.getOrdersOfUser(any())).thenReturn(List.of());
        when(webPageService.findUserId(any())).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("สิทธิ์เข้าถึงหน้าและเมนู navbar")
    class AccessAndNavbar {

        @Test
        @DisplayName("guest: เข้าหน้าแรกและแกลเลอรีได้ เห็นปุ่มเข้าสู่ระบบ ไม่เห็นเมนูของพนักงาน")
        void guest_CanBrowsePublicPages_AndSeesSignInOnly() throws Exception {
            for (String page : List.of("/", "/cards")) {
                mockMvc.perform(get(page))
                        .andExpect(status().isOk())
                        .andExpect(content().string(containsString("href=\"/login\"")))
                        .andExpect(content().string(not(containsString("href=\"/inventory\""))))
                        .andExpect(content().string(not(containsString("href=\"/orders\""))))
                        .andExpect(content().string(not(containsString("action=\"/logout\""))));
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {"/inventory", "/accounts", "/orders", "/my-orders"})
        @DisplayName("guest: หน้าที่ต้องล็อกอินถูกส่งไปหน้า /login")
        void guest_IsRedirectedToLogin_FromProtectedPages(String page) throws Exception {
            mockMvc.perform(get(page))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/login"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/inventory", "/accounts", "/orders"})
        @WithMockUser(username = "customer_red", roles = "CUSTOMER")
        @DisplayName("CUSTOMER: เข้าหน้าของพนักงานไม่ได้ (403)")
        void customer_IsForbidden_FromStaffPages(String page) throws Exception {
            mockMvc.perform(get(page)).andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "customer_red", roles = "CUSTOMER")
        @DisplayName("CUSTOMER: เห็นเมนูคำสั่งซื้อของฉัน ชื่อผู้ใช้ และปุ่มออกจากระบบ แต่ไม่เห็นเมนูพนักงาน")
        void customer_SeesMyOrdersAndSignOut_ButNotStaffMenu() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("href=\"/my-orders\"")))
                    .andExpect(content().string(containsString("customer_red")))
                    .andExpect(content().string(containsString("action=\"/logout\"")))
                    .andExpect(content().string(not(containsString("href=\"/inventory\""))))
                    .andExpect(content().string(not(containsString("href=\"/login\""))));
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("STAFF: เห็นเมนูคลัง/บัญชีเกม/คำสั่งซื้อ และฟอร์มออกจากระบบมี CSRF token แต่ไม่เห็นลิงก์ Swagger")
        void staff_SeesStaffMenu_AndCsrfProtectedSignOut() throws Exception {
            mockMvc.perform(get("/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("inventory"))
                    .andExpect(content().string(containsString("href=\"/inventory\"")))
                    .andExpect(content().string(containsString("href=\"/accounts\"")))
                    .andExpect(content().string(containsString("href=\"/orders\"")))
                    .andExpect(content().string(containsString("staff_ash")))
                    .andExpect(content().string(containsString("action=\"/logout\"")))
                    .andExpect(content().string(containsString("name=\"_csrf\"")))
                    .andExpect(content().string(not(containsString("swagger-ui.html"))))
                    .andExpect(content().string(not(containsString("href=\"/my-orders\""))));
        }

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("ADMIN: เห็นลิงก์ Swagger ใน navbar")
        void admin_SeesSwaggerLink() throws Exception {
            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("swagger-ui.html")));
        }
    }

    @Nested
    @DisplayName("หน้า /login และการเข้า/ออกจากระบบ")
    class LoginPage {

        // ข้อความในกล่องแจ้งเตือนของ login.html (ฟอร์มสมัครสมาชิกมีกล่อง class เดียวกันซ่อนอยู่ จึงเช็กจากข้อความแทน)
        private static final String BAD_CREDENTIALS_MESSAGE = "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง";
        private static final String LOGGED_OUT_MESSAGE = "ออกจากระบบเรียบร้อยแล้ว";

        @Test
        @DisplayName("หน้า /login: ฟอร์มส่ง POST /login พร้อมช่อง username, password และ CSRF token")
        void loginForm_PostsUsernamePasswordWithCsrf() throws Exception {
            mockMvc.perform(get("/login"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("action=\"/login\"")))
                    .andExpect(content().string(containsString("name=\"username\"")))
                    .andExpect(content().string(containsString("name=\"password\"")))
                    .andExpect(content().string(containsString("name=\"_csrf\"")))
                    .andExpect(content().string(not(containsString(BAD_CREDENTIALS_MESSAGE))))
                    .andExpect(content().string(not(containsString(LOGGED_OUT_MESSAGE))));
        }

        @Test
        @DisplayName("หน้า /login?error=true: แสดงกล่องแจ้งรหัสผ่านผิด")
        void loginPage_WithErrorParam_ShowsErrorBox() throws Exception {
            mockMvc.perform(get("/login").param("error", "true"))
                    .andExpect(content().string(containsString(BAD_CREDENTIALS_MESSAGE)));
        }

        @Test
        @DisplayName("หน้า /login?logout=true: แสดงกล่องแจ้งออกจากระบบสำเร็จ")
        void loginPage_WithLogoutParam_ShowsLogoutBox() throws Exception {
            mockMvc.perform(get("/login").param("logout", "true"))
                    .andExpect(content().string(containsString(LOGGED_OUT_MESSAGE)));
        }

        @Test
        @DisplayName("ล็อกอินถูกต้องไปหน้าแรก, รหัสผิดกลับไป /login?error=true")
        void formLogin_RedirectsOnSuccessAndFailure() throws Exception {
            when(userDetailsService.loadUserByUsername("staff_ash")).thenReturn(
                    org.springframework.security.core.userdetails.User.withUsername("staff_ash")
                            .password(new BCryptPasswordEncoder().encode("password123"))
                            .roles("STAFF")
                            .build());

            mockMvc.perform(formLogin("/login").user("staff_ash").password("password123"))
                    .andExpect(redirectedUrl("/"));
            mockMvc.perform(formLogin("/login").user("staff_ash").password("wrong-password"))
                    .andExpect(redirectedUrl("/login?error=true"));
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("ปุ่มออกจากระบบ (POST /logout) ส่งไป /login?logout=true")
        void signOut_RedirectsToLoginWithLogoutMessage() throws Exception {
            mockMvc.perform(post("/logout").with(csrf()))
                    .andExpect(redirectedUrl("/login?logout=true"));
        }
    }

    @Nested
    @DisplayName("ข้อมูลจาก WebPageService ลง Model และ render ด้วย template จริง")
    class PageModels {

        @Test
        @DisplayName("หน้าแรก: ใส่ตัวเลขสรุปและการ์ด Spotlight จาก service")
        void dashboard_PutsServiceDataInModel() throws Exception {
            List<CardView> featured = List.of(CHARIZARD);
            when(webPageService.getDashboard()).thenReturn(DashboardPage.builder()
                    .totalCards(28).totalExpansions(2).totalOrders(5).featuredCards(featured).build());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("dashboard"))
                    .andExpect(model().attribute("featuredCards", sameInstance(featured)))
                    .andExpect(model().attribute("totalCards", 28L))
                    .andExpect(model().attribute("totalExpansions", 2L))
                    .andExpect(model().attribute("totalOrders", 5L))
                    .andExpect(content().string(containsString("Charizard ex")));
        }

        @Test
        @DisplayName("/cards: ส่ง parameter ตัวกรองให้ service ตามที่รับมา และใส่ผลลัพธ์ทุกตัวลง Model")
        void cards_ForwardsFiltersAndPutsGalleryInModel() throws Exception {
            List<CardView> cards = List.of(CHARIZARD);
            List<FilterOption> rarityPills = List.of(FilterOption.builder().name("CROWN_RARE").label("Crown Rare")
                    .active(true).url("/cards?element=FIRE&rarity=CROWN_RARE&search=char").build());
            when(webPageService.getCardGallery("FIRE", "crown_rare", "POKEMON", "char")).thenReturn(CardGalleryPage.builder()
                    .cards(cards).selectedElement(ElementType.FIRE).selectedRarity(Rarity.CROWN_RARE)
                    .selectedType(CardType.POKEMON).search("char")
                    .allElementsUrl("/cards?rarity=CROWN_RARE&type=POKEMON&search=char").elementFilters(List.of())
                    .allRaritiesUrl("/cards?element=FIRE&type=POKEMON&search=char").rarityFilters(rarityPills)
                    .allTypesUrl("/cards?element=FIRE&rarity=CROWN_RARE&search=char").typeFilters(List.of())
                    .build());

            mockMvc.perform(get("/cards").param("element", "FIRE").param("rarity", "crown_rare")
                            .param("type", "POKEMON").param("search", "char"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cards"))
                    .andExpect(model().attribute("cards", sameInstance(cards)))
                    .andExpect(model().attribute("totalElements", 1))
                    .andExpect(model().attribute("selectedElement", ElementType.FIRE))
                    .andExpect(model().attribute("selectedRarity", Rarity.CROWN_RARE))
                    .andExpect(model().attribute("selectedType", CardType.POKEMON))
                    .andExpect(model().attribute("search", "char"))
                    .andExpect(model().attribute("rarityFilters", sameInstance(rarityPills)))
                    .andExpect(model().attribute("allElementsUrl", "/cards?rarity=CROWN_RARE&type=POKEMON&search=char"))
                    .andExpect(content().string(containsString("Charizard ex")))
                    .andExpect(content().string(containsString("Crown Rare")));

            verify(webPageService).getCardGallery("FIRE", "crown_rare", "POKEMON", "char");
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("/inventory: ใส่สต็อกและรายชื่อลูกค้าจาก service")
        void inventory_PutsServiceDataInModel() throws Exception {
            List<InventoryView> rows = List.of(InventoryView.builder().id(22L).cardName("Charizard ex")
                    .cardNumber("280/226").expansionCode("A1").imageUrl("/images/cards/A1_280_EN.png")
                    .condition(CardCondition.MINT).quantity(2).lowStock(true).outOfStock(false)
                    .buyInPrice(new BigDecimal("900.00")).sellingPrice(new BigDecimal("1500.00")).storageSlot("A-01")
                    .build());
            List<CustomerView> customers = List.of(RED);
            when(webPageService.getInventoryPage()).thenReturn(InventoryPage.builder()
                    .inventories(rows).customers(customers).build());

            mockMvc.perform(get("/inventory"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("inventories", sameInstance(rows)))
                    .andExpect(model().attribute("totalElements", 1))
                    .andExpect(model().attribute("customers", sameInstance(customers)))
                    .andExpect(content().string(containsString("Charizard ex")))
                    .andExpect(content().string(containsString("Red (VIP)")));
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("/accounts: ใส่บัญชีเกม สรุปสถานะ ลูกค้า และระดับสมาชิกทั้งหมด")
        void accounts_PutsServiceDataInModel() throws Exception {
            List<GameAccountView> accounts = List.of(GameAccountView.builder().id(1L).accountCode("PV-ACC-01")
                    .inGameName("VaultKeeper").friendId("1111-2222-3333-4444").tradeStatus(AccountTradeStatus.READY)
                    .buyInCost(new BigDecimal("300.00")).notes("main").totalCardsCount(7).build());
            List<CustomerView> customers = List.of(RED);
            when(webPageService.getAccountsPage()).thenReturn(AccountsPage.builder()
                    .accounts(accounts).readyAccounts(1).cooldownAccounts(0).customers(customers).build());

            mockMvc.perform(get("/accounts"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("accounts", sameInstance(accounts)))
                    .andExpect(model().attribute("totalAccounts", 1))
                    .andExpect(model().attribute("readyAccounts", 1L))
                    .andExpect(model().attribute("cooldownAccounts", 0L))
                    .andExpect(model().attribute("customers", sameInstance(customers)))
                    .andExpect(model().attribute("membershipTiers", MembershipTier.values()))
                    .andExpect(content().string(containsString("PV-ACC-01")));
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("/orders: ใส่ออเดอร์ทั้งหมดของร้านจาก service")
        void orders_PutsAllOrdersInModel() throws Exception {
            List<OrderView> orders = List.of(ORDER);
            when(webPageService.getAllOrders()).thenReturn(orders);

            mockMvc.perform(get("/orders"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("orders", sameInstance(orders)))
                    .andExpect(content().string(containsString("ORD-2026-009")))
                    .andExpect(content().string(containsString("2026-10-09 14:30")));
        }

        @Test
        @WithMockUser(username = "customer_red", roles = "CUSTOMER")
        @DisplayName("/my-orders: ขอเฉพาะออเดอร์ของผู้ใช้ที่ล็อกอิน ไม่ดึงออเดอร์ทั้งร้าน")
        void myOrders_AsksOnlyForSignedInUsersOrders() throws Exception {
            List<OrderView> orders = List.of(ORDER);
            when(webPageService.getOrdersOfUser("customer_red")).thenReturn(orders);

            mockMvc.perform(get("/my-orders"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("my-orders"))
                    .andExpect(model().attribute("orders", sameInstance(orders)))
                    .andExpect(content().string(containsString("ORD-2026-009")));

            verify(webPageService).getOrdersOfUser("customer_red");
            verify(webPageService, never()).getAllOrders();
        }

        @Test
        @WithMockUser(username = "customer_red", roles = "CUSTOMER")
        @DisplayName("currentUserId: ผู้ใช้ที่ล็อกอินได้ id จาก service (ใช้กับฟอร์มสั่งซื้อ)")
        void currentUserId_ForSignedInUser() throws Exception {
            when(webPageService.findUserId("customer_red")).thenReturn(Optional.of(7L));

            mockMvc.perform(get("/cards")).andExpect(model().attribute("currentUserId", 7L));
        }

        @Test
        @DisplayName("currentUserId: guest ได้ null และไม่เรียก service")
        void currentUserId_ForGuest_IsNull() throws Exception {
            mockMvc.perform(get("/cards")).andExpect(model().attribute("currentUserId", nullValue()));

            verify(webPageService, never()).findUserId(any());
        }
    }
}
