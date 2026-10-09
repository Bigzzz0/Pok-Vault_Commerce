package com.pokevault.modules.order.dto;

import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long id;
    private Long inventoryId;
    private Long cardId;
    private String cardName;
    private String cardNumber;
    private String cardImageUrl;
    private Long assignedAccountId;
    private TradeFulfillmentStatus tradeStatus;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public static OrderItemResponse fromEntity(OrderItem item) {
        if (item == null) {
            return null;
        }

        Long inventoryId = item.getInventory() != null ? item.getInventory().getId() : null;
        Long cardId = null;
        String cardName = null;
        String cardNumber = null;
        String cardImageUrl = null;

        if (item.getInventory() != null && item.getInventory().getCard() != null) {
            cardId = item.getInventory().getCard().getId();
            cardName = item.getInventory().getCard().getName();
            cardNumber = item.getInventory().getCard().getCardNumber();
            cardImageUrl = item.getInventory().getCard().getImageUrl();
        }

        Long assignedAccountId = item.getAssignedAccount() != null ? item.getAssignedAccount().getId() : null;

        return OrderItemResponse.builder()
                .id(item.getId())
                .inventoryId(inventoryId)
                .cardId(cardId)
                .cardName(cardName)
                .cardNumber(cardNumber)
                .cardImageUrl(cardImageUrl)
                .assignedAccountId(assignedAccountId)
                .tradeStatus(item.getTradeStatus())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
