package com.pokevault.modules.web;

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
import com.pokevault.modules.web.dto.view.AccountsPage;
import com.pokevault.modules.web.dto.view.CardGalleryPage;
import com.pokevault.modules.web.dto.view.CardView;
import com.pokevault.modules.web.dto.view.CustomerView;
import com.pokevault.modules.web.dto.view.DashboardPage;
import com.pokevault.modules.web.dto.view.FilterOption;
import com.pokevault.modules.web.dto.view.GameAccountView;
import com.pokevault.modules.web.dto.view.InventoryView;
import com.pokevault.modules.web.dto.view.OrderView;
import com.pokevault.modules.web.service.GuestCustomerInitializer;
import com.pokevault.modules.web.service.WebPageServiceImpl;
import com.pokevault.modules.web.service.WebViewMapper;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ WebPageServiceImpl: กฎของร้านที่ใช้ประกอบข้อมูลหน้าเว็บ
 * (แสดงเฉพาะการ์ดที่มีสต็อก, ตัวกรองแกลเลอรี, เลือกล็อตที่ขาย, กรองลูกค้า, ออเดอร์ของผู้ใช้)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 5 (Frontend & Chat Commerce)
 */
@ExtendWith(MockitoExtension.class)
class WebPageServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardExpansionRepository expansionRepository;
    @Mock
    private CardInventoryRepository inventoryRepository;
    @Mock
    private GameAccountRepository gameAccountRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;

    private WebPageServiceImpl service;

    private static final CardExpansion A1 = CardExpansion.builder().id(1L).code("A1").name("Genetic Apex").build();

    @BeforeEach
    void setUp() {
        service = new WebPageServiceImpl(cardRepository, expansionRepository, inventoryRepository,
                gameAccountRepository, orderRepository, userRepository, new WebViewMapper());
    }

    @Nested
    @DisplayName("แกลเลอรีการ์ด getCardGallery")
    class CardGallery {

        private final Card venusaur = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
        private final Card charizard = card(2L, "035/226", "Charizard", Rarity.RARE, ElementType.FIRE, CardType.POKEMON);
        private final Card charmander = card(3L, "033/226", "Charmander", Rarity.COMMON, ElementType.FIRE, CardType.POKEMON);
        private final Card sabrina = card(4L, "225/226", "Sabrina", Rarity.UNCOMMON, null, CardType.TRAINER_SUPPORTER);

        @BeforeEach
        void givenCatalog() {
            when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(venusaur, charizard, charmander, sabrina));
            when(inventoryRepository.findAll()).thenReturn(List.of(
                    inventory(10L, venusaur, 3, "180.00"),
                    inventory(11L, charizard, 5, "60.00"),
                    inventory(12L, charmander, 0, "10.00"),
                    inventory(13L, sabrina, 2, "40.00")));
        }

        @Test
        @DisplayName("แสดงเฉพาะการ์ดที่มีสต็อก > 0")
        void showsOnlyCardsInStock() {
            assertThat(names(service.getCardGallery(null, null, null, null)))
                    .containsExactly("Venusaur ex", "Charizard", "Sabrina");
        }

        @Test
        @DisplayName("กรองธาตุ (ไม่สนตัวพิมพ์): การ์ด Trainer ที่ไม่มีธาตุนับเป็น COLORLESS")
        void filterByElement_TreatsTrainerAsColorless() {
            assertThat(names(service.getCardGallery("fire", null, null, null))).containsExactly("Charizard");
            assertThat(names(service.getCardGallery("COLORLESS", null, null, null))).containsExactly("Sabrina");
        }

        @Test
        @DisplayName("ตัวกรอง rarity, ประเภท และคำค้น (ไม่สนตัวพิมพ์/ช่องว่าง) ใช้ร่วมกันได้")
        void filtersCombine() {
            assertThat(names(service.getCardGallery(null, "DOUBLE_RARE", null, null))).containsExactly("Venusaur ex");
            assertThat(names(service.getCardGallery(null, null, "TRAINER_SUPPORTER", null))).containsExactly("Sabrina");
            assertThat(names(service.getCardGallery(null, null, null, "  CHAR "))).containsExactly("Charizard");
            assertThat(names(service.getCardGallery("GRASS", null, null, "char"))).isEmpty();
        }

        @Test
        @DisplayName("ค่าตัวกรองที่ไม่มีใน enum ถูกเมิน แสดงการ์ดทั้งหมดแทน error")
        void unknownFilterValue_IsIgnored() {
            CardGalleryPage page = service.getCardGallery("x", "MEGA_RARE", null, null);

            assertThat(page.getCards()).hasSize(3);
            assertThat(page.getSelectedElement()).isNull();
            assertThat(page.getSelectedRarity()).isNull();
        }

        @Test
        @DisplayName("ลิงก์ pill ตัวกรองแต่ละอันคงตัวกรองแถวอื่นและคำค้นไว้")
        void filterPillUrls_KeepOtherFiltersAndSearch() {
            CardGalleryPage page = service.getCardGallery("FIRE", null, null, "char");

            FilterOption doubleRare = page.getRarityFilters().stream()
                    .filter(p -> p.getName().equals("DOUBLE_RARE")).findFirst().orElseThrow();
            assertThat(doubleRare.getUrl()).isEqualTo("/cards?element=FIRE&rarity=DOUBLE_RARE&search=char");
            assertThat(doubleRare.getLabel()).isEqualTo("Double Rare");
            assertThat(page.getAllElementsUrl()).isEqualTo("/cards?search=char");
            assertThat(page.getElementFilters()).filteredOn(FilterOption::isActive)
                    .extracting(FilterOption::getName).containsExactly("FIRE");
            assertThat(page.getElementFilters()).filteredOn(p -> p.getName().equals("FIRE"))
                    .extracting(FilterOption::getLabel).containsExactly("ไฟ");
        }
    }

    @Test
    @DisplayName("ราคาที่แสดงมาจากล็อตที่ถูกที่สุดที่ยังมีของ และนับสต็อกรวมทุกล็อต")
    void price_ComesFromCheapestInventoryWithStock() {
        Card charizard = card(2L, "035/226", "Charizard", Rarity.RARE, ElementType.FIRE, CardType.POKEMON);
        when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(charizard));
        when(inventoryRepository.findAll()).thenReturn(List.of(
                inventory(20L, charizard, 1, "300.00"),
                inventory(21L, charizard, 0, "250.00"),
                inventory(22L, charizard, 2, "280.00")));

        CardView view = service.getCardGallery(null, null, null, null).getCards().get(0);

        assertThat(view.getPrice()).isEqualByComparingTo("280.00");
        assertThat(view.getOrderInventoryId()).isEqualTo(22L);
        assertThat(view.getTotalStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("รูปการ์ดใช้ไฟล์ใน static/images/cards ถ้ามี ไม่มีก็ใช้หลังการ์ด")
    void imageUrl_UsesBundledImageOrCardBack() {
        Card venusaur = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
        Card noImage = card(9L, "999/226", "Missing Image", Rarity.COMMON, ElementType.WATER, CardType.POKEMON);
        when(cardRepository.findAll(any(Sort.class))).thenReturn(List.of(venusaur, noImage));
        when(inventoryRepository.findAll()).thenReturn(List.of(
                inventory(10L, venusaur, 1, "180.00"), inventory(30L, noImage, 1, "5.00")));

        assertThat(service.getCardGallery(null, null, null, null).getCards()).extracting(CardView::getImageUrl)
                .containsExactly("/images/cards/A1_004_EN.png", "/images/cards/card-back.jpg");
    }

    @Test
    @DisplayName("หน้าแรก: Spotlight แสดงเฉพาะการ์ดที่มีสต็อก ไม่เกิน 6 ใบ และนับจำนวนการ์ดทั้งแคตตาล็อก")
    void dashboard_FeaturedCards_InStockOnly_MaxSix() {
        List<Card> cards = new ArrayList<>();
        List<CardInventory> stock = new ArrayList<>();
        for (long id = 1; id <= 8; id++) {
            Card c = card(id, String.format("%03d/226", id), "Card " + id, Rarity.COMMON, ElementType.WATER, CardType.POKEMON);
            cards.add(c);
            // การ์ดใบที่ 1 หมดสต็อก จึงต้องไม่ขึ้น Spotlight
            stock.add(inventory(100 + id, c, id == 1 ? 0 : 1, "10.00"));
        }
        when(cardRepository.findAll(any(Sort.class))).thenReturn(cards);
        when(inventoryRepository.findAll()).thenReturn(stock);
        when(cardRepository.count()).thenReturn(28L);

        DashboardPage page = service.getDashboard();

        assertThat(page.getFeaturedCards()).extracting(CardView::getName)
                .containsExactly("Card 2", "Card 3", "Card 4", "Card 5", "Card 6", "Card 7");
        assertThat(page.getTotalCards()).isEqualTo(28L);
    }

    @Test
    @DisplayName("คลัง: ฟอร์มจองแสดงเฉพาะลูกค้า โดยบัญชี Facebook Guest อยู่อันดับแรก")
    void inventory_BookingCustomers_OnlyCustomers_GuestFirst() {
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(
                user(1L, "admin", UserRole.ADMIN),
                user(2L, "customer_red", UserRole.CUSTOMER),
                user(3L, GuestCustomerInitializer.GUEST_USERNAME, UserRole.CUSTOMER)));

        assertThat(service.getInventoryPage().getCustomers()).extracting(CustomerView::getUsername)
                .containsExactly(GuestCustomerInitializer.GUEST_USERNAME, "customer_red");
    }

    @Test
    @DisplayName("คลัง: ติดป้ายสต็อกใกล้หมด (≤ 2) และหมดสต็อก")
    void inventory_FlagsLowAndOutOfStock() {
        Card c = card(1L, "004/226", "Venusaur ex", Rarity.DOUBLE_RARE, ElementType.GRASS, CardType.POKEMON);
        when(inventoryRepository.findAll(any(Sort.class))).thenReturn(List.of(
                inventory(1L, c, 5, "180.00"), inventory(2L, c, 2, "180.00"), inventory(3L, c, 0, "180.00")));

        List<InventoryView> rows = service.getInventoryPage().getInventories();

        assertThat(rows).extracting(InventoryView::isLowStock).containsExactly(false, true, true);
        assertThat(rows).extracting(InventoryView::isOutOfStock).containsExactly(false, false, true);
    }

    @Test
    @DisplayName("บัญชีเกม: นับสถานะ READY/COOLDOWN และจำนวนการ์ดในแต่ละบัญชี; ตารางลูกค้าไม่รวม Guest")
    void accounts_CountsStatusesAndCardsPerAccount() {
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

        AccountsPage page = service.getAccountsPage();

        assertThat(page.getReadyAccounts()).isEqualTo(1L);
        assertThat(page.getCooldownAccounts()).isEqualTo(1L);
        assertThat(page.getAccounts()).extracting(GameAccountView::getTotalCardsCount).containsExactly(7, 0);
        assertThat(page.getCustomers()).extracting(CustomerView::getUsername).containsExactly("customer_red");
    }

    @Test
    @DisplayName("คำสั่งซื้อของฉัน: แสดงเฉพาะออเดอร์ของผู้ใช้คนนั้น เรียงใหม่สุดก่อน")
    void ordersOfUser_OnlyThatUsersOrders_NewestFirst() {
        User red = user(7L, "customer_red", UserRole.CUSTOMER);
        when(userRepository.findByUsername("customer_red")).thenReturn(Optional.of(red));
        when(orderRepository.findByUserId(7L)).thenReturn(List.of(
                Order.builder().id(3L).orderCode("ORD-003").user(red).build(),
                Order.builder().id(9L).orderCode("ORD-009").user(red).build()));

        assertThat(service.getOrdersOfUser("customer_red")).extracting(OrderView::getOrderCode)
                .containsExactly("ORD-009", "ORD-003");
        verify(orderRepository, never()).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("คำสั่งซื้อของฉัน: ผู้ใช้ที่ไม่มีในระบบได้รายการว่าง ไม่เห็นออเดอร์ของคนอื่น")
    void ordersOfUser_UnknownUser_ReturnsEmpty() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThat(service.getOrdersOfUser("ghost")).isEmpty();
        verify(orderRepository, never()).findByUserId(any());
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

    private static List<String> names(CardGalleryPage page) {
        return page.getCards().stream().map(CardView::getName).toList();
    }
}
