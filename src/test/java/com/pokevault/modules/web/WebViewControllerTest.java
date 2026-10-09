package com.pokevault.modules.web;

import com.pokevault.common.security.CustomUserDetailsService;
import com.pokevault.common.security.SecurityConfig;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardExpansion;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.domain.enums.UserRole;
import com.pokevault.modules.web.controller.WebViewController;
import com.pokevault.modules.web.service.GuestCustomerInitializer;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web MVC Test สำหรับหน้าเว็บ Thymeleaf (WebViewController) + เมนู/สิทธิ์ตามบทบาทผู้ใช้
 * ใช้ repository แบบ mock ไม่แตะฐานข้อมูล จึงไม่ขึ้นกับ data.sql
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@WebMvcTest(WebViewController.class)
@Import(SecurityConfig.class)
class WebViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardRepository cardRepository;
    @MockitoBean
    private CardExpansionRepository expansionRepository;
    @MockitoBean
    private CardInventoryRepository inventoryRepository;
    @MockitoBean
    private GameAccountRepository gameAccountRepository;
    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    // @EnableJpaAuditing บน PokevaultApplication ต้องการ JPA metamodel ซึ่ง @WebMvcTest ไม่ได้โหลด
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final CardExpansion A1 = CardExpansion.builder().id(1L).code("A1").name("Genetic Apex").build();

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
    @DisplayName("แกลเลอรีการ์ด /cards")
    class CardGallery {

        private final Card venusaur = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
        private final Card charizard = card(2L, "035/226", "Charizard", Rarity.RARE, ElementType.FIRE, CardType.POKEMON);
        private final Card charmander = card(3L, "033/226", "Charmander", Rarity.COMMON, ElementType.FIRE, CardType.POKEMON);
        private final Card sabrina = card(4L, "225/226", "Sabrina", Rarity.UNCOMMON, null, CardType.TRAINER_SUPPORTER);

        private void givenCatalog() {
            when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(venusaur, charizard, charmander, sabrina));
            when(inventoryRepository.findAll()).thenReturn(List.of(
                    inventory(10L, venusaur, 3, "180.00"),
                    inventory(11L, charizard, 5, "60.00"),
                    inventory(12L, charmander, 0, "10.00"),
                    inventory(13L, sabrina, 2, "40.00")));
        }

        @Test
        @DisplayName("แสดงเฉพาะการ์ดที่มีสต็อก > 0")
        void showsOnlyCardsInStock() throws Exception {
            givenCatalog();

            MvcResult result = mockMvc.perform(get("/cards")).andExpect(status().isOk()).andReturn();

            assertThat(cardNames(result)).containsExactly("Venusaur ex", "Charizard", "Sabrina");
            assertThat(result.getModelAndView().getModel().get("totalElements")).isEqualTo(3);
        }

        @Test
        @DisplayName("กรองธาตุ: การ์ด Trainer ที่ไม่มีธาตุนับเป็น COLORLESS")
        void filterByElement_TreatsTrainerAsColorless() throws Exception {
            givenCatalog();

            assertThat(cardNames(mockMvc.perform(get("/cards").param("element", "fire")).andReturn()))
                    .containsExactly("Charizard");
            assertThat(cardNames(mockMvc.perform(get("/cards").param("element", "COLORLESS")).andReturn()))
                    .containsExactly("Sabrina");
        }

        @Test
        @DisplayName("ตัวกรอง rarity, ประเภท และคำค้น (ไม่สนตัวพิมพ์/ช่องว่าง) ใช้ร่วมกันได้")
        void filtersCombine() throws Exception {
            givenCatalog();

            assertThat(cardNames(mockMvc.perform(get("/cards").param("rarity", "DOUBLE_RARE")).andReturn()))
                    .containsExactly("Venusaur ex");
            assertThat(cardNames(mockMvc.perform(get("/cards").param("type", "TRAINER_SUPPORTER")).andReturn()))
                    .containsExactly("Sabrina");
            assertThat(cardNames(mockMvc.perform(get("/cards").param("search", "  CHAR ")).andReturn()))
                    .containsExactly("Charizard");
            assertThat(cardNames(mockMvc.perform(get("/cards").param("search", "char").param("element", "GRASS")).andReturn()))
                    .isEmpty();
        }

        @Test
        @DisplayName("ค่าตัวกรองที่ไม่มีใน enum ถูกเมิน แสดงการ์ดทั้งหมดแทน error")
        void unknownFilterValue_IsIgnored() throws Exception {
            givenCatalog();

            MvcResult result = mockMvc.perform(get("/cards").param("rarity", "MEGA_RARE").param("element", "x"))
                    .andExpect(status().isOk()).andReturn();

            assertThat(cardNames(result)).hasSize(3);
            assertThat(result.getModelAndView().getModel().get("selectedRarity")).isNull();
            assertThat(result.getModelAndView().getModel().get("selectedElement")).isNull();
        }

        @Test
        @DisplayName("ลิงก์ pill ตัวกรองแต่ละอันคงตัวกรองแถวอื่นและคำค้นไว้")
        @SuppressWarnings("unchecked")
        void filterPillUrls_KeepOtherFiltersAndSearch() throws Exception {
            givenCatalog();

            Map<String, Object> model = mockMvc.perform(get("/cards").param("element", "FIRE").param("search", "char"))
                    .andReturn().getModelAndView().getModel();

            List<Map<String, Object>> rarityPills = (List<Map<String, Object>>) model.get("rarityFilters");
            Map<String, Object> doubleRare = rarityPills.stream()
                    .filter(p -> p.get("name").equals("DOUBLE_RARE")).findFirst().orElseThrow();
            assertThat(doubleRare.get("url")).isEqualTo("/cards?element=FIRE&rarity=DOUBLE_RARE&search=char");
            assertThat(doubleRare.get("label")).isEqualTo("Double Rare");
            assertThat(model.get("allElementsUrl")).isEqualTo("/cards?search=char");

            List<Map<String, Object>> elementPills = (List<Map<String, Object>>) model.get("elementFilters");
            assertThat(elementPills).filteredOn(p -> (boolean) p.get("active"))
                    .extracting(p -> p.get("name")).containsExactly("FIRE");
        }

        @Test
        @DisplayName("ราคาที่แสดงมาจากล็อตที่ถูกที่สุดที่ยังมีของ และนับสต็อกรวมทุกล็อต")
        void price_ComesFromCheapestInventoryWithStock() throws Exception {
            when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(charizard));
            when(inventoryRepository.findAll()).thenReturn(List.of(
                    inventory(20L, charizard, 1, "300.00"),
                    inventory(21L, charizard, 0, "250.00"),
                    inventory(22L, charizard, 2, "280.00")));

            Map<String, Object> view = cardViews(mockMvc.perform(get("/cards")).andReturn()).get(0);

            assertThat((BigDecimal) view.get("price")).isEqualByComparingTo("280.00");
            assertThat(view.get("orderInventoryId")).isEqualTo(22L);
            assertThat(view.get("totalStock")).isEqualTo(3);
        }

        @Test
        @DisplayName("รูปการ์ดใช้ไฟล์ใน static/images/cards ถ้ามี ไม่มีก็ใช้หลังการ์ด")
        void imageUrl_UsesBundledImageOrCardBack() throws Exception {
            Card noImage = card(9L, "999/226", "Missing Image", Rarity.COMMON, ElementType.WATER, CardType.POKEMON);
            when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(venusaur, noImage));
            when(inventoryRepository.findAll()).thenReturn(List.of(
                    inventory(10L, venusaur, 1, "180.00"), inventory(30L, noImage, 1, "5.00")));

            List<Map<String, Object>> views = cardViews(mockMvc.perform(get("/cards")).andReturn());

            assertThat(views).extracting(v -> v.get("imageUrl"))
                    .containsExactly("/images/cards/A1_004_EN.png", "/images/cards/card-back.jpg");
        }
    }

    @Nested
    @DisplayName("หน้าแรก / คลัง / บัญชีเกม / คำสั่งซื้อของฉัน")
    class OtherPages {

        @Test
        @DisplayName("หน้าแรก: Spotlight แสดงเฉพาะการ์ดที่มีสต็อก ไม่เกิน 6 ใบ")
        void dashboard_FeaturedCards_InStockOnly_MaxSix() throws Exception {
            List<Card> cards = new java.util.ArrayList<>();
            List<CardInventory> stock = new java.util.ArrayList<>();
            for (long id = 1; id <= 8; id++) {
                Card c = card(id, String.format("%03d/226", id), "Card " + id, Rarity.COMMON, ElementType.WATER, CardType.POKEMON);
                cards.add(c);
                // การ์ดใบที่ 1 หมดสต็อก จึงต้องไม่ขึ้น Spotlight
                stock.add(inventory(100 + id, c, id == 1 ? 0 : 1, "10.00"));
            }
            when(cardRepository.findAll(any(Sort.class))).thenReturn(cards);
            when(inventoryRepository.findAll()).thenReturn(stock);
            when(cardRepository.count()).thenReturn(28L);

            MvcResult result = mockMvc.perform(get("/")).andExpect(view().name("dashboard")).andReturn();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> featured = (List<Map<String, Object>>) result.getModelAndView().getModel().get("featuredCards");
            assertThat(featured).extracting(v -> v.get("name"))
                    .containsExactly("Card 2", "Card 3", "Card 4", "Card 5", "Card 6", "Card 7");
            assertThat(result.getModelAndView().getModel().get("totalCards")).isEqualTo(28L);
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("คลัง: ฟอร์มจองแสดงเฉพาะลูกค้า โดยบัญชี Facebook Guest อยู่อันดับแรก")
        @SuppressWarnings("unchecked")
        void inventory_BookingCustomers_OnlyCustomers_GuestFirst() throws Exception {
            when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(
                    user(1L, "admin", UserRole.ADMIN),
                    user(2L, "customer_red", UserRole.CUSTOMER),
                    user(3L, GuestCustomerInitializer.GUEST_USERNAME, UserRole.CUSTOMER)));

            Map<String, Object> model = mockMvc.perform(get("/inventory")).andReturn().getModelAndView().getModel();

            assertThat((List<Map<String, Object>>) model.get("customers")).extracting(c -> c.get("username"))
                    .containsExactly(GuestCustomerInitializer.GUEST_USERNAME, "customer_red");
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("คลัง: ติดป้ายสต็อกใกล้หมด (≤ 2) และหมดสต็อก")
        @SuppressWarnings("unchecked")
        void inventory_FlagsLowAndOutOfStock() throws Exception {
            Card c = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
            when(inventoryRepository.findAll(any(Sort.class))).thenReturn(List.of(
                    inventory(1L, c, 5, "180.00"), inventory(2L, c, 2, "180.00"), inventory(3L, c, 0, "180.00")));

            List<Map<String, Object>> rows = (List<Map<String, Object>>) mockMvc.perform(get("/inventory"))
                    .andReturn().getModelAndView().getModel().get("inventories");

            assertThat(rows).extracting(r -> r.get("lowStock")).containsExactly(false, true, true);
            assertThat(rows).extracting(r -> r.get("outOfStock")).containsExactly(false, false, true);
        }

        @Test
        @WithMockUser(username = "staff_ash", roles = "STAFF")
        @DisplayName("บัญชีเกม: นับสถานะ READY/COOLDOWN และจำนวนการ์ดในแต่ละบัญชี; ตารางลูกค้าไม่รวม Guest")
        @SuppressWarnings("unchecked")
        void accounts_CountsStatusesAndCardsPerAccount() throws Exception {
            GameAccount ready = GameAccount.builder().id(1L).accountCode("PV-ACC-01").tradeStatus(AccountTradeStatus.READY).build();
            GameAccount cooldown = GameAccount.builder().id(2L).accountCode("PV-ACC-02").tradeStatus(AccountTradeStatus.COOLDOWN).build();
            Card c = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
            CardInventory a = inventory(1L, c, 3, "180.00");
            a.setGameAccount(ready);
            CardInventory b = inventory(2L, c, 4, "180.00");
            b.setGameAccount(ready);
            when(gameAccountRepository.findAll(any(Sort.class))).thenReturn(List.of(ready, cooldown));
            when(inventoryRepository.findAll()).thenReturn(List.of(a, b));
            when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(
                    user(2L, "customer_red", UserRole.CUSTOMER),
                    user(3L, GuestCustomerInitializer.GUEST_USERNAME, UserRole.CUSTOMER)));

            Map<String, Object> model = mockMvc.perform(get("/accounts")).andExpect(status().isOk())
                    .andReturn().getModelAndView().getModel();

            assertThat(model.get("readyAccounts")).isEqualTo(1L);
            assertThat(model.get("cooldownAccounts")).isEqualTo(1L);
            assertThat((List<Map<String, Object>>) model.get("accounts")).extracting(v -> v.get("totalCardsCount"))
                    .containsExactly(7, 0);
            assertThat((List<Map<String, Object>>) model.get("customers")).extracting(v -> v.get("username"))
                    .containsExactly("customer_red");
        }

        @Test
        @WithMockUser(username = "customer_red", roles = "CUSTOMER")
        @DisplayName("คำสั่งซื้อของฉัน: แสดงเฉพาะออเดอร์ของผู้ใช้ที่ล็อกอิน เรียงใหม่สุดก่อน")
        @SuppressWarnings("unchecked")
        void myOrders_ShowsOnlySignedInUsersOrders_NewestFirst() throws Exception {
            User red = user(7L, "customer_red", UserRole.CUSTOMER);
            when(userRepository.findByUsername("customer_red")).thenReturn(Optional.of(red));
            when(orderRepository.findByUserId(7L)).thenReturn(List.of(
                    Order.builder().id(3L).orderCode("ORD-003").user(red).build(),
                    Order.builder().id(9L).orderCode("ORD-009").user(red).build()));

            Map<String, Object> model = mockMvc.perform(get("/my-orders")).andExpect(status().isOk())
                    .andReturn().getModelAndView().getModel();

            assertThat((List<Map<String, Object>>) model.get("orders")).extracting(o -> o.get("orderCode"))
                    .containsExactly("ORD-009", "ORD-003");
            verify(orderRepository, never()).findAll(any(Sort.class));
        }
    }

    private static Card card(Long id, String number, String name, Rarity rarity, ElementType element, CardType type) {
        return Card.builder().id(id).expansion(A1).cardNumber(number).name(name)
                .rarity(rarity).elementType(element).cardType(type).hp(100).build();
    }

    private static CardInventory inventory(Long id, Card card, int quantity, String price) {
        return CardInventory.builder().id(id).card(card).quantity(quantity).sellingPrice(new BigDecimal(price)).build();
    }

    private static User user(Long id, String username, UserRole role) {
        return User.builder().id(id).username(username).email(username + "@pokevault.local").role(role).build();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> cardViews(MvcResult result) {
        return (List<Map<String, Object>>) result.getModelAndView().getModel().get("cards");
    }

    private static List<Object> cardNames(MvcResult result) {
        return cardViews(result).stream().map(v -> v.get("name")).toList();
    }
}
