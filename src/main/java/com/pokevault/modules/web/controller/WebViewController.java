package com.pokevault.modules.web.controller;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.modules.vault.observer.LowStockObserver;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderRepository;
import com.pokevault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serves the Thymeleaf pages under templates/. Entities are mapped to plain
 * view-model maps here so the templates get the fields they expect
 * (e.g. totalStock, imageUrl, membershipTier) without touching the API DTOs.
 */
@Controller
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebViewController {

    private static final int FEATURED_CARD_LIMIT = 6;
    private static final String CARD_IMAGE_DIR = "/images/cards/";
    private static final String CARD_BACK_IMAGE = CARD_IMAGE_DIR + "card-back.jpg";

    private final CardRepository cardRepository;
    private final CardExpansionRepository expansionRepository;
    private final CardInventoryRepository inventoryRepository;
    private final GameAccountRepository gameAccountRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /** Id of the signed-in user for the customer order form; null for guests. */
    @ModelAttribute("currentUserId")
    public Long currentUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName()).map(User::getId).orElse(null);
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<CardInventory> inventories = inventoryRepository.findAll();
        Map<Long, Integer> stockByCard = stockByCard(inventories);
        Map<Long, CardInventory> offerByCard = offerByCard(inventories);

        List<Map<String, Object>> featured = cardRepository.findAll(Sort.by(Sort.Direction.DESC, "rarity")).stream()
                .filter(c -> isInVault(c, stockByCard))
                .limit(FEATURED_CARD_LIMIT)
                .map(c -> toCardView(c, stockByCard, offerByCard.get(c.getId())))
                .toList();

        model.addAttribute("totalCards", cardRepository.count());
        model.addAttribute("totalExpansions", expansionRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("featuredCards", featured);
        return "dashboard";
    }

    @GetMapping("/cards")
    public String cards(@RequestParam(required = false) String element,
                        @RequestParam(required = false) String rarity,
                        @RequestParam(required = false) String type,
                        @RequestParam(required = false) String search,
                        Model model) {
        ElementType selectedElement = parseEnum(ElementType.class, element);
        Rarity selectedRarity = parseEnum(Rarity.class, rarity);
        CardType selectedType = parseEnum(CardType.class, type);
        String keyword = search == null ? "" : search.trim().toLowerCase();
        List<CardInventory> inventories = inventoryRepository.findAll();
        Map<Long, Integer> stockByCard = stockByCard(inventories);
        Map<Long, CardInventory> offerByCard = offerByCard(inventories);

        List<Map<String, Object>> cards = cardRepository.findAll(Sort.by("expansion.code", "cardNumber")).stream()
                .filter(c -> isInVault(c, stockByCard))
                // Trainer cards have no element and are displayed as COLORLESS (see toCardView)
                .filter(c -> selectedElement == null || selectedElement
                        == (c.getElementType() != null ? c.getElementType() : ElementType.COLORLESS))
                .filter(c -> selectedRarity == null || c.getRarity() == selectedRarity)
                .filter(c -> selectedType == null || c.getCardType() == selectedType)
                .filter(c -> keyword.isEmpty() || c.getName().toLowerCase().contains(keyword))
                .map(c -> toCardView(c, stockByCard, offerByCard.get(c.getId())))
                .toList();

        model.addAttribute("cards", cards);
        model.addAttribute("totalElements", cards.size());
        model.addAttribute("selectedElement", selectedElement);
        model.addAttribute("selectedRarity", selectedRarity);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("search", search);
        // each pill keeps the other filters and the search term, so the filters combine
        model.addAttribute("allElementsUrl", cardsUrl(null, selectedRarity, selectedType, search));
        model.addAttribute("elementFilters", Arrays.stream(ElementType.values())
                .map(e -> toFilterView(e, e == selectedElement, cardsUrl(e, selectedRarity, selectedType, search)))
                .toList());
        model.addAttribute("allRaritiesUrl", cardsUrl(selectedElement, null, selectedType, search));
        model.addAttribute("rarityFilters", Arrays.stream(Rarity.values())
                .map(r -> toFilterView(r, r == selectedRarity, cardsUrl(selectedElement, r, selectedType, search)))
                .toList());
        model.addAttribute("allTypesUrl", cardsUrl(selectedElement, selectedRarity, null, search));
        model.addAttribute("typeFilters", Arrays.stream(CardType.values())
                .map(t -> toFilterView(t, t == selectedType, cardsUrl(selectedElement, selectedRarity, t, search)))
                .toList());
        return "cards";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<Map<String, Object>> inventories = inventoryRepository.findAll(Sort.by("id")).stream()
                .map(this::toInventoryView)
                .toList();

        List<Map<String, Object>> customers = userRepository.findAll(Sort.by("id")).stream()
                .map(this::toCustomerView)
                .toList();

        model.addAttribute("inventories", inventories);
        model.addAttribute("totalElements", inventories.size());
        model.addAttribute("customers", customers);
        return "inventory";
    }

    @GetMapping("/accounts")
    public String accounts(Model model) {
        Map<Long, Integer> cardsByAccount = inventoryRepository.findAll().stream()
                .filter(inv -> inv.getGameAccount() != null && inv.getQuantity() != null)
                .collect(Collectors.groupingBy(inv -> inv.getGameAccount().getId(),
                        Collectors.summingInt(CardInventory::getQuantity)));

        List<GameAccount> entities = gameAccountRepository.findAll(Sort.by("id"));
        List<Map<String, Object>> accounts = entities.stream()
                .map(a -> toAccountView(a, cardsByAccount.getOrDefault(a.getId(), 0)))
                .toList();

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalAccounts", entities.size());
        model.addAttribute("readyAccounts", countByStatus(entities, AccountTradeStatus.READY));
        model.addAttribute("cooldownAccounts", countByStatus(entities, AccountTradeStatus.COOLDOWN));
        model.addAttribute("totalElements", entities.size());
        return "accounts";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        List<Map<String, Object>> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toOrderView)
                .toList();

        model.addAttribute("orders", orders);
        return "orders";
    }

    /** Read-only order history of the signed-in user; /orders stays the staff console for every order. */
    @GetMapping("/my-orders")
    public String myOrders(Principal principal, Model model) {
        List<Map<String, Object>> orders = userRepository.findByUsername(principal.getName())
                .map(user -> orderRepository.findByUserId(user.getId()))
                .orElse(List.of()).stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(this::toMyOrderView)
                .toList();

        model.addAttribute("orders", orders);
        return "my-orders";
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

    private Map<String, Object> toFilterView(Enum<?> value, boolean active, String url) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("name", value.name());
        // DOUBLE_RARE -> "Double Rare"
        view.put("label", Arrays.stream(value.name().split("_"))
                .map(w -> w.charAt(0) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" ")));
        view.put("active", active);
        view.put("url", url);
        return view;
    }

    /** The storefront only shows cards the store actually holds; catalog entries with no stock stay hidden. */
    private boolean isInVault(Card card, Map<Long, Integer> stockByCard) {
        return stockByCard.getOrDefault(card.getId(), 0) > 0;
    }

    private Map<Long, Integer> stockByCard(List<CardInventory> inventories) {
        return inventories.stream()
                .filter(inv -> inv.getCard() != null && inv.getQuantity() != null)
                .collect(Collectors.groupingBy(inv -> inv.getCard().getId(),
                        Collectors.summingInt(CardInventory::getQuantity)));
    }

    /** The stock row a customer order is placed against: the cheapest one that still has copies. */
    private Map<Long, CardInventory> offerByCard(List<CardInventory> inventories) {
        return inventories.stream()
                .filter(inv -> inv.getCard() != null && inv.getQuantity() != null && inv.getQuantity() > 0)
                .filter(inv -> inv.getSellingPrice() != null)
                .collect(Collectors.toMap(inv -> inv.getCard().getId(), inv -> inv,
                        (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b));
    }

    private long countByStatus(List<GameAccount> accounts, AccountTradeStatus status) {
        return accounts.stream().filter(a -> a.getTradeStatus() == status).count();
    }

    /**
     * The seeded image URLs point at a remote host that rejects hotlinking (403), so prefer the
     * bundled static image (e.g. A1_004_EN.png) and fall back to the card back when none exists.
     */
    private String resolveImageUrl(Card card) {
        if (card.getExpansion() != null && card.getCardNumber() != null) {
            // card numbers are stored as "004/226"; the bundled files only use the leading part
            String number = String.format("%3s", card.getCardNumber().split("/")[0].trim()).replace(' ', '0');
            String code = card.getExpansion().getCode();
            for (String prefix : List.of(code, code.toUpperCase())) {
                String file = prefix + "_" + number + "_EN.png";
                if (new ClassPathResource("static" + CARD_IMAGE_DIR + file).exists()) {
                    return CARD_IMAGE_DIR + file;
                }
            }
        }
        return CARD_BACK_IMAGE;
    }

    private Map<String, Object> toCardView(Card card, Map<Long, Integer> stockByCard, CardInventory offer) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", card.getId());
        view.put("name", card.getName());
        view.put("cardNumber", card.getCardNumber());
        view.put("expansionCode", card.getExpansion() != null ? card.getExpansion().getCode() : null);
        view.put("rarity", card.getRarity());
        view.put("cardType", card.getCardType());
        view.put("rarityDescription", null);
        // Trainer/Item cards have no element, but the templates call elementType.name() unconditionally
        view.put("elementType", card.getElementType() != null ? card.getElementType() : ElementType.COLORLESS);
        view.put("hp", card.getHp());
        view.put("description", null);
        view.put("imageUrl", resolveImageUrl(card));
        view.put("totalStock", stockByCard.getOrDefault(card.getId(), 0));
        view.put("orderInventoryId", offer != null ? offer.getId() : null);
        view.put("price", offer != null ? offer.getSellingPrice() : null);
        return view;
    }

    private Map<String, Object> toInventoryView(CardInventory inv) {
        Card card = inv.getCard();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", inv.getId());
        view.put("cardName", card != null ? card.getName() : null);
        view.put("cardNumber", card != null ? card.getCardNumber() : null);
        view.put("expansionCode", card != null && card.getExpansion() != null ? card.getExpansion().getCode() : null);
        view.put("imageUrl", card != null ? resolveImageUrl(card) : CARD_BACK_IMAGE);
        view.put("condition", inv.getCondition());
        view.put("conditionLabel", inv.getCondition());
        view.put("quantity", inv.getQuantity());
        // same threshold the backend observer alerts on, so the page and the logs agree
        view.put("lowStock", inv.getQuantity() != null && inv.getQuantity() <= LowStockObserver.LOW_STOCK_THRESHOLD);
        view.put("outOfStock", inv.getQuantity() == null || inv.getQuantity() <= 0);
        view.put("buyInPrice", inv.getBuyInPrice());
        view.put("sellingPrice", inv.getSellingPrice());
        view.put("storageSlot", inv.getStorageSlot());
        return view;
    }

    private Map<String, Object> toAccountView(GameAccount account, int totalCards) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", account.getId());
        view.put("accountCode", account.getAccountCode());
        view.put("inGameName", account.getInGameName());
        view.put("friendId", account.getFriendId());
        view.put("tradeStatus", account.getTradeStatus());
        view.put("buyInCost", account.getBuyInCost());
        view.put("notes", account.getNotes());
        view.put("totalCardsCount", totalCards);
        return view;
    }

    private Map<String, Object> toCustomerView(User user) {
        UserProfile profile = user.getUserProfile();
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("displayName", profile != null && profile.getFullName() != null ? profile.getFullName() : user.getUsername());
        view.put("membershipTier", profile != null && profile.getMembershipTier() != null
                ? profile.getMembershipTier() : MembershipTier.REGULAR);
        return view;
    }

    private Map<String, Object> toOrderView(Order order) {
        User user = order.getUser();
        UserProfile profile = user != null ? user.getUserProfile() : null;

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", order.getId());
        view.put("orderCode", order.getOrderCode());
        view.put("createdAt", order.getCreatedAt());
        view.put("username", user != null ? user.getUsername() : null);
        view.put("customerFullName", profile != null ? profile.getFullName() : null);
        view.put("customerFriendId", order.getCustomerFriendId());
        view.put("customerInGameName", order.getCustomerInGameName());
        view.put("membershipTier", profile != null ? profile.getMembershipTier() : null);
        view.put("orderStatus", order.getOrderStatus());
        view.put("items", order.getItems());
        view.put("discountAmount", order.getDiscountAmount());
        view.put("finalAmount", order.getFinalAmount());
        return view;
    }

    private Map<String, Object> toMyOrderView(Order order) {
        Map<String, Object> view = toOrderView(order);
        view.put("items", order.getItems().stream().map(this::toOrderItemView).toList());
        return view;
    }

    private Map<String, Object> toOrderItemView(OrderItem item) {
        Card card = item.getInventory() != null ? item.getInventory().getCard() : null;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("cardName", card != null ? card.getName() : "Unknown card");
        view.put("imageUrl", card != null ? resolveImageUrl(card) : CARD_BACK_IMAGE);
        view.put("quantity", item.getQuantity());
        view.put("unitPrice", item.getUnitPrice());
        return view;
    }
}
