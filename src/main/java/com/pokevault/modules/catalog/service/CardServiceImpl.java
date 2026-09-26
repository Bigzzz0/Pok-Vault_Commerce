package com.pokevault.modules.catalog.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardExpansion;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardResponse;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardExpansionRepository cardExpansionRepository;

    @Override
    public List<CardResponse> getAllCards(CardFilterRequest filter) {
        List<Card> cards = cardRepository.findAll();

        if (filter != null) {
            if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                cards = cards.stream()
                        .filter(c -> c.getName().toLowerCase().contains(filter.getName().toLowerCase().trim()))
                        .collect(Collectors.toList());
            }
            if (filter.getExpansionCode() != null && !filter.getExpansionCode().trim().isEmpty()) {
                cards = cards.stream()
                        .filter(c -> c.getExpansion() != null &&
                                c.getExpansion().getCode().equalsIgnoreCase(filter.getExpansionCode().trim()))
                        .collect(Collectors.toList());
            }
            if (filter.getRarity() != null && !filter.getRarity().trim().isEmpty()) {
                try {
                    Rarity rarityEnum = Rarity.valueOf(filter.getRarity().trim().toUpperCase());
                    cards = cards.stream()
                            .filter(c -> c.getRarity() == rarityEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) {}
            }
            if (filter.getCardType() != null && !filter.getCardType().trim().isEmpty()) {
                try {
                    CardType typeEnum = CardType.valueOf(filter.getCardType().trim().toUpperCase());
                    cards = cards.stream()
                            .filter(c -> c.getCardType() == typeEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) {}
            }
            if (filter.getElementType() != null && !filter.getElementType().trim().isEmpty()) {
                try {
                    ElementType elementEnum = ElementType.valueOf(filter.getElementType().trim().toUpperCase());
                    cards = cards.stream()
                            .filter(c -> c.getElementType() == elementEnum)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException ignored) {}
            }
        }

        return cards.stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CardResponse getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));
        return CardResponse.fromEntity(card);
    }

    @Override
    public List<CardResponse> searchCards(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCards(null);
        }
        return cardRepository.findByNameContainingIgnoreCase(query.trim()).stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CardExpansionResponse> getAllExpansions() {
        return cardExpansionRepository.findAll().stream()
                .map(CardExpansionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CardExpansionResponse getExpansionByCode(String code) {
        CardExpansion expansion = cardExpansionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("CardExpansion", "code", code));
        return CardExpansionResponse.fromEntity(expansion);
    }

    @Override
    public List<CardResponse> getCardsByExpansionCode(String expansionCode) {
        return cardRepository.findByExpansionCode(expansionCode).stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
