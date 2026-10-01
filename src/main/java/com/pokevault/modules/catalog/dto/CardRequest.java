package com.pokevault.modules.catalog.dto;

import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    @NotNull(message = "Expansion ID is required")
    private Long expansionId;

    @NotBlank(message = "Card number is required")
    @Size(max = 20, message = "Card number must not exceed 20 characters")
    private String cardNumber;

    @NotBlank(message = "Card name is required")
    @Size(max = 100, message = "Card name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Card type is required")
    private CardType cardType;

    @NotNull(message = "Rarity is required")
    private Rarity rarity;

    private ElementType elementType;

    @Min(value = 0, message = "HP must be zero or positive")
    private Integer hp;

    @Min(value = 0, message = "Retreat cost must be zero or positive")
    private Integer retreatCost;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}
