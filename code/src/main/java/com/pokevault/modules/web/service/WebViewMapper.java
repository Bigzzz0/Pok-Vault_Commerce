package com.pokevault.modules.web.service;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.entity.User;
import com.pokevault.domain.entity.UserProfile;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.MembershipTier;
import com.pokevault.modules.vault.observer.LowStockObserver;
import com.pokevault.modules.web.dto.view.CardView;
import com.pokevault.modules.web.dto.view.CustomerView;
import com.pokevault.modules.web.dto.view.FilterOption;
import com.pokevault.modules.web.dto.view.GameAccountView;
import com.pokevault.modules.web.dto.view.InventoryView;
import com.pokevault.modules.web.dto.view.OrderItemView;
import com.pokevault.modules.web.dto.view.OrderView;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * แปลง Entity เป็น View DTO สำหรับหน้าเว็บ Thymeleaf (ไม่แตะฐานข้อมูล)
 * แยกออกจาก WebPageServiceImpl เพื่อให้ service ดูแลเฉพาะการดึงข้อมูลและกฎของร้าน
 */
@Component
public class WebViewMapper {

    static final String CARD_IMAGE_DIR = "/images/cards/";
    static final String CARD_BACK_IMAGE = CARD_IMAGE_DIR + "card-back.jpg";

    private static final Map<String, String> THAI_FILTER_LABELS = Map.ofEntries(
            Map.entry("FIRE", "ไฟ"), Map.entry("WATER", "น้ำ"), Map.entry("GRASS", "หญ้า"),
            Map.entry("LIGHTNING", "สายฟ้า"), Map.entry("PSYCHIC", "พลังจิต"), Map.entry("FIGHTING", "ต่อสู้"),
            Map.entry("DARKNESS", "ความมืด"), Map.entry("METAL", "โลหะ"), Map.entry("DRAGON", "มังกร"),
            Map.entry("COLORLESS", "ไร้สี"), Map.entry("POKEMON", "โปเกมอน"),
            Map.entry("TRAINER_SUPPORTER", "เทรนเนอร์ ซัพพอร์ต"), Map.entry("TRAINER_ITEM", "เทรนเนอร์ ไอเท็ม"));

    public CardView toCardView(Card card, int totalStock, CardInventory offer) {
        return CardView.builder()
                .id(card.getId())
                .name(card.getName())
                .cardNumber(card.getCardNumber())
                .expansionCode(card.getExpansion() != null ? card.getExpansion().getCode() : null)
                .rarity(card.getRarity())
                .cardType(card.getCardType())
                // Trainer/Item cards have no element, but the templates call elementType.name() unconditionally
                .elementType(displayElement(card))
                .hp(card.getHp())
                .imageUrl(resolveImageUrl(card))
                .totalStock(totalStock)
                .orderInventoryId(offer != null ? offer.getId() : null)
                .price(offer != null ? offer.getSellingPrice() : null)
                .build();
    }

    /** Trainer cards have no element and are displayed (and filtered) as COLORLESS */
    public ElementType displayElement(Card card) {
        return card.getElementType() != null ? card.getElementType() : ElementType.COLORLESS;
    }

    public FilterOption toFilterOption(Enum<?> value, boolean active, String url) {
        // Thai label for elements / card types; rarities keep the printed name (DOUBLE_RARE -> "Double Rare")
        String label = THAI_FILTER_LABELS.getOrDefault(value.name(), Arrays.stream(value.name().split("_"))
                .map(w -> w.charAt(0) + w.substring(1).toLowerCase())
                .collect(Collectors.joining(" ")));
        return FilterOption.builder().name(value.name()).label(label).active(active).url(url).build();
    }

    public InventoryView toInventoryView(CardInventory inv) {
        Card card = inv.getCard();
        return InventoryView.builder()
                .id(inv.getId())
                .cardName(card != null ? card.getName() : null)
                .cardNumber(card != null ? card.getCardNumber() : null)
                .expansionCode(card != null && card.getExpansion() != null ? card.getExpansion().getCode() : null)
                .imageUrl(card != null ? resolveImageUrl(card) : CARD_BACK_IMAGE)
                .condition(inv.getCondition())
                .quantity(inv.getQuantity())
                // same threshold the backend observer alerts on, so the page and the logs agree
                .lowStock(inv.getQuantity() != null && inv.getQuantity() <= LowStockObserver.LOW_STOCK_THRESHOLD)
                .outOfStock(inv.getQuantity() == null || inv.getQuantity() <= 0)
                .buyInPrice(inv.getBuyInPrice())
                .sellingPrice(inv.getSellingPrice())
                .storageSlot(inv.getStorageSlot())
                .build();
    }

    public GameAccountView toAccountView(GameAccount account, int totalCards) {
        return GameAccountView.builder()
                .id(account.getId())
                .accountCode(account.getAccountCode())
                .inGameName(account.getInGameName())
                .friendId(account.getFriendId())
                .tradeStatus(account.getTradeStatus())
                .buyInCost(account.getBuyInCost())
                .notes(account.getNotes())
                .totalCardsCount(totalCards)
                .build();
    }

    public CustomerView toCustomerView(User user) {
        UserProfile profile = user.getUserProfile();
        return CustomerView.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(profile != null && profile.getFullName() != null ? profile.getFullName() : user.getUsername())
                .membershipTier(profile != null && profile.getMembershipTier() != null
                        ? profile.getMembershipTier() : MembershipTier.REGULAR)
                .build();
    }

    public OrderView toOrderView(Order order) {
        User user = order.getUser();
        UserProfile profile = user != null ? user.getUserProfile() : null;
        return OrderView.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(order.getCreatedAt())
                .username(user != null ? user.getUsername() : null)
                .customerFullName(profile != null ? profile.getFullName() : null)
                .customerFriendId(order.getCustomerFriendId())
                .customerInGameName(order.getCustomerInGameName())
                .membershipTier(profile != null ? profile.getMembershipTier() : null)
                .orderStatus(order.getOrderStatus())
                .items(order.getItems().stream().map(this::toOrderItemView).toList())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .build();
    }

    private OrderItemView toOrderItemView(OrderItem item) {
        Card card = item.getInventory() != null ? item.getInventory().getCard() : null;
        return OrderItemView.builder()
                .cardName(card != null ? card.getName() : "Unknown card")
                .imageUrl(card != null ? resolveImageUrl(card) : CARD_BACK_IMAGE)
                .expansionCode(card != null && card.getExpansion() != null ? card.getExpansion().getCode() : null)
                .cardNumber(card != null ? card.getCardNumber() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }

    /**
     * The seeded image URLs point at a remote host that rejects hotlinking (403), so prefer the
     * bundled static image (e.g. A1_004_EN.png) and fall back to the card back when none exists.
     */
    String resolveImageUrl(Card card) {
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
}
