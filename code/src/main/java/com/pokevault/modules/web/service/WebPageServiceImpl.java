package com.pokevault.modules.web.service;

import com.pokevault.domain.entity.Card;
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
import com.pokevault.modules.web.dto.view.InventoryPage;
import com.pokevault.modules.web.dto.view.OrderView;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebPageServiceImpl implements WebPageService {

    static final int FEATURED_CARD_LIMIT = 6;

    private final CardRepository cardRepository;
    private final CardExpansionRepository expansionRepository;
    private final CardInventoryRepository inventoryRepository;
    private final GameAccountRepository gameAccountRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final WebViewMapper mapper;

    @Override
    public Optional<Long> findUserId(String username) {
        return userRepository.findByUsername(username).map(User::getId);
    }

    @Override
    public DashboardPage getDashboard() {
        StockSnapshot stock = StockSnapshot.of(inventoryRepository.findAll(), mapper);
        List<CardView> featured = cardRepository.findAll(Sort.by(Sort.Direction.DESC, "rarity")).stream()
                .filter(stock::isInVault)
                .limit(FEATURED_CARD_LIMIT)
                .map(stock::toCardView)
                .toList();

        return DashboardPage.builder()
                .totalCards(cardRepository.count())
                .totalExpansions(expansionRepository.count())
                .totalOrders(orderRepository.count())
                .featuredCards(featured)
                .build();
    }

    @Override
    public CardGalleryPage getCardGallery(String element, String rarity, String type, String search) {
        ElementType selectedElement = parseEnum(ElementType.class, element);
        Rarity selectedRarity = parseEnum(Rarity.class, rarity);
        CardType selectedType = parseEnum(CardType.class, type);
        String keyword = search == null ? "" : search.trim().toLowerCase();
        StockSnapshot stock = StockSnapshot.of(inventoryRepository.findAll(), mapper);

        List<CardView> cards = cardRepository.findAll(Sort.by("expansion.code", "cardNumber")).stream()
                .filter(stock::isInVault)
                .filter(c -> selectedElement == null || selectedElement == mapper.displayElement(c))
                .filter(c -> selectedRarity == null || c.getRarity() == selectedRarity)
                .filter(c -> selectedType == null || c.getCardType() == selectedType)
                .filter(c -> keyword.isEmpty() || c.getName().toLowerCase().contains(keyword))
                .map(stock::toCardView)
                .toList();

        // each pill keeps the other filters and the search term, so the filters combine
        return CardGalleryPage.builder()
                .cards(cards)
                .selectedElement(selectedElement)
                .selectedRarity(selectedRarity)
                .selectedType(selectedType)
                .search(search)
                .allElementsUrl(cardsUrl(null, selectedRarity, selectedType, search))
                .elementFilters(filterOptions(ElementType.values(), selectedElement,
                        e -> cardsUrl(e, selectedRarity, selectedType, search)))
                .allRaritiesUrl(cardsUrl(selectedElement, null, selectedType, search))
                .rarityFilters(filterOptions(Rarity.values(), selectedRarity,
                        r -> cardsUrl(selectedElement, r, selectedType, search)))
                .allTypesUrl(cardsUrl(selectedElement, selectedRarity, null, search))
                .typeFilters(filterOptions(CardType.values(), selectedType,
                        t -> cardsUrl(selectedElement, selectedRarity, t, search)))
                .build();
    }

    @Override
    public InventoryPage getInventoryPage() {
        // staff book on behalf of customers only; the shared guest account (Facebook orders) comes first as the default
        List<CustomerView> customers = customers().stream()
                .sorted(Comparator.comparing((User u) -> !isGuest(u)))
                .map(mapper::toCustomerView)
                .toList();

        return InventoryPage.builder()
                .inventories(inventoryRepository.findAll(Sort.by("id")).stream().map(mapper::toInventoryView).toList())
                .customers(customers)
                .build();
    }

    @Override
    public AccountsPage getAccountsPage() {
        Map<Long, Integer> cardsByAccount = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getGameAccount() != null && inv.getQuantity() != null)
                .collect(Collectors.groupingBy(inv -> inv.getGameAccount().getId(),
                        Collectors.summingInt(CardInventory::getQuantity)));
        List<GameAccount> accounts = gameAccountRepository.findAll(Sort.by("id"));

        return AccountsPage.builder()
                .accounts(accounts.stream()
                        .map(a -> mapper.toAccountView(a, cardsByAccount.getOrDefault(a.getId(), 0)))
                        .toList())
                .readyAccounts(countByStatus(accounts, AccountTradeStatus.READY))
                .cooldownAccounts(countByStatus(accounts, AccountTradeStatus.COOLDOWN))
                // staff set each customer's membership tier here; the shared guest has no tier to manage
                .customers(customers().stream().filter(u -> !isGuest(u)).map(mapper::toCustomerView).toList())
                .build();
    }

    @Override
    public List<OrderView> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(mapper::toOrderView)
                .toList();
    }

    @Override
    public List<OrderView> getOrdersOfUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> orderRepository.findByUserId(user.getId()))
                .orElse(List.of()).stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(mapper::toOrderView)
                .toList();
    }

    private List<User> customers() {
        return userRepository.findAll(Sort.by("id")).stream()
                .filter(u -> u.getRole() == UserRole.CUSTOMER)
                .toList();
    }

    private boolean isGuest(User user) {
        return GuestCustomerInitializer.GUEST_USERNAME.equals(user.getUsername());
    }

    private long countByStatus(List<GameAccount> accounts, AccountTradeStatus status) {
        return accounts.stream().filter(a -> a.getTradeStatus() == status).count();
    }

    private <E extends Enum<E>> List<FilterOption> filterOptions(E[] values, E selected, Function<E, String> urlOf) {
        return Arrays.stream(values)
                .map(v -> mapper.toFilterOption(v, v == selected, urlOf.apply(v)))
                .toList();
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String cardsUrl(ElementType element, Rarity rarity, CardType type, String search) {
        return UriComponentsBuilder.fromPath("/cards")
                .queryParamIfPresent("element", Optional.ofNullable(element))
                .queryParamIfPresent("rarity", Optional.ofNullable(rarity))
                .queryParamIfPresent("type", Optional.ofNullable(type))
                .queryParamIfPresent("search", Optional.ofNullable(search).filter(v -> !v.isBlank()))
                .build().encode().toUriString();
    }

    /**
     * สต็อกรวมต่อการ์ด และล็อตที่ใช้ขายให้ลูกค้า (ล็อตที่ถูกที่สุดที่ยังมีของ) คำนวณครั้งเดียวต่อการโหลดหน้า
     */
    private record StockSnapshot(Map<Long, Integer> stockByCard, Map<Long, CardInventory> offerByCard,
                                 WebViewMapper mapper) {

        static StockSnapshot of(List<CardInventory> inventories, WebViewMapper mapper) {
            Map<Long, Integer> stock = inventories.stream()
                    .filter(inv -> inv.getCard() != null && inv.getQuantity() != null)
                    .collect(Collectors.groupingBy(inv -> inv.getCard().getId(),
                            Collectors.summingInt(CardInventory::getQuantity)));
            Map<Long, CardInventory> offers = inventories.stream()
                    .filter(inv -> inv.getCard() != null && inv.getQuantity() != null && inv.getQuantity() > 0)
                    .filter(inv -> inv.getSellingPrice() != null)
                    .collect(Collectors.toMap(inv -> inv.getCard().getId(), inv -> inv,
                            (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b));
            return new StockSnapshot(stock, offers, mapper);
        }

        /** The storefront only shows cards the store actually holds; catalog entries with no stock stay hidden. */
        boolean isInVault(Card card) {
            return stockByCard.getOrDefault(card.getId(), 0) > 0;
        }

        CardView toCardView(Card card) {
            return mapper.toCardView(card, stockByCard.getOrDefault(card.getId(), 0), offerByCard.get(card.getId()));
        }
    }
}
