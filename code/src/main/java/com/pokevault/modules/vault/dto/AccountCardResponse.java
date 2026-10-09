package com.pokevault.modules.vault.dto;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.enums.CardCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO สำหรับแสดงข้อมูลการ์ดที่เก็บอยู่ในไอดีเกม (ตาม Class Diagram & Sequence
 * Diagram ข้อ 14)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCardResponse {

    private Long inventoryId;
    private Long cardId;
    private String cardNumber;
    private String cardName;
    private String expansionCode;
    private String rarity;
    private String elementType;
    private String imageUrl;
    private CardCondition condition;
    private int quantity;
    private BigDecimal buyInPrice;
    private BigDecimal sellingPrice;
    private String storageSlot;

    public static AccountCardResponse fromEntity(CardInventory inventory) {
        if (inventory == null)
            return null;
        Card card = inventory.getCard();
        return AccountCardResponse.builder()
                .inventoryId(inventory.getId())
                .cardId(card != null ? card.getId() : null)
                .cardNumber(card != null ? card.getCardNumber() : null)
                .cardName(card != null ? card.getName() : null)
                .expansionCode(card != null && card.getExpansion() != null ? card.getExpansion().getCode() : null)
                .rarity(card != null && card.getRarity() != null ? card.getRarity().name() : null)
                .elementType(card != null && card.getElementType() != null ? card.getElementType().name() : null)
                .imageUrl(card != null ? card.getImageUrl() : null)
                .condition(inventory.getCondition())
                .quantity(inventory.getQuantity())
                .buyInPrice(inventory.getBuyInPrice())
                .sellingPrice(inventory.getSellingPrice())
                .storageSlot(inventory.getStorageSlot())
                .build();
    }
}
