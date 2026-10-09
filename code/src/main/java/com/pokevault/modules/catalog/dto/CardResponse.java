package com.pokevault.modules.catalog.dto;

import com.pokevault.domain.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private String expansionCode;
    private String expansionName;
    private String cardNumber;
    private String name;
    private String cardType;
    private String rarity;
    private String elementType;
    private Integer hp;
    private Integer retreatCost;
    private String imageUrl;

    public static CardResponse fromEntity(Card card) {
        if (card == null) return null;
        return CardResponse.builder()
                .id(card.getId())
                .expansionCode(card.getExpansion() != null ? card.getExpansion().getCode() : null)
                .expansionName(card.getExpansion() != null ? card.getExpansion().getName() : null)
                .cardNumber(card.getCardNumber())
                .name(card.getName())
                .cardType(card.getCardType() != null ? card.getCardType().name() : null)
                .rarity(card.getRarity() != null ? card.getRarity().name() : null)
                .elementType(card.getElementType() != null ? card.getElementType().name() : null)
                .hp(card.getHp())
                .retreatCost(card.getRetreatCost())
                .imageUrl(card.getImageUrl())
                .build();
    }
}
