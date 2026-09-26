package com.pokevault.modules.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardFilterRequest {

    private String name;
    private String expansionCode;
    private String rarity;
    private String cardType;
    private String elementType;
}
