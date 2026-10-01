package com.pokevault.modules.catalog.dto;

import com.pokevault.domain.entity.CardExpansion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardExpansionResponse {

    private Long id;
    private String code;
    private String name;
    private String series;
    private LocalDate releaseDate;
    private Integer totalCards;

    public static CardExpansionResponse fromEntity(CardExpansion expansion) {
        if (expansion == null) return null;
        return CardExpansionResponse.builder()
                .id(expansion.getId())
                .code(expansion.getCode())
                .name(expansion.getName())
                .series(expansion.getSeries())
                .releaseDate(expansion.getReleaseDate())
                .totalCards(expansion.getTotalCards())
                .build();
    }
}
