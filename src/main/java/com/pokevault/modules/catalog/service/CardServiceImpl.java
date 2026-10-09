package com.pokevault.modules.catalog.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.response.PageResponse;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardExpansion;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.modules.catalog.dto.CardExpansionResponse;
import com.pokevault.modules.catalog.dto.CardFilterRequest;
import com.pokevault.modules.catalog.dto.CardRequest;
import com.pokevault.modules.catalog.dto.CardResponse;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Override
    public PageResponse<CardResponse> getCardsPaged(CardFilterRequest filter, Pageable pageable) {
        Specification<Card> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter != null) {
                if (filter.getName() != null && !filter.getName().trim().isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().trim().toLowerCase() + "%"));
                }
                if (filter.getExpansionCode() != null && !filter.getExpansionCode().trim().isEmpty()) {
                    predicates.add(cb.equal(cb.upper(root.join("expansion").get("code")), filter.getExpansionCode().trim().toUpperCase()));
                }
                if (filter.getRarity() != null && !filter.getRarity().trim().isEmpty()) {
                    try {
                        Rarity rarityEnum = Rarity.valueOf(filter.getRarity().trim().toUpperCase());
                        predicates.add(cb.equal(root.get("rarity"), rarityEnum));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (filter.getCardType() != null && !filter.getCardType().trim().isEmpty()) {
                    try {
                        CardType typeEnum = CardType.valueOf(filter.getCardType().trim().toUpperCase());
                        predicates.add(cb.equal(root.get("cardType"), typeEnum));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (filter.getElementType() != null && !filter.getElementType().trim().isEmpty()) {
                    try {
                        ElementType elementEnum = ElementType.valueOf(filter.getElementType().trim().toUpperCase());
                        predicates.add(cb.equal(root.get("elementType"), elementEnum));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Card> cardPage = cardRepository.findAll(spec, pageable);
        List<CardResponse> content = cardPage.getContent().stream()
                .map(CardResponse::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.<CardResponse>builder()
                .content(content)
                .pageNumber(cardPage.getNumber())
                .pageSize(cardPage.getSize())
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .isFirst(cardPage.isFirst())
                .isLast(cardPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public CardResponse createCard(CardRequest request) {
        CardExpansion expansion = cardExpansionRepository.findById(request.getExpansionId())
                .orElseThrow(() -> new ResourceNotFoundException("CardExpansion", "id", request.getExpansionId()));

        Card card = Card.builder()
                .expansion(expansion)
                .cardNumber(request.getCardNumber())
                .name(request.getName())
                .cardType(request.getCardType())
                .rarity(request.getRarity())
                .elementType(request.getElementType())
                .hp(request.getHp())
                .retreatCost(request.getRetreatCost())
                .imageUrl(request.getImageUrl())
                .build();

        Card savedCard = cardRepository.save(card);
        return CardResponse.fromEntity(savedCard);
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long id, CardRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));

        CardExpansion expansion = cardExpansionRepository.findById(request.getExpansionId())
                .orElseThrow(() -> new ResourceNotFoundException("CardExpansion", "id", request.getExpansionId()));

        card.setExpansion(expansion);
        card.setCardNumber(request.getCardNumber());
        card.setName(request.getName());
        card.setCardType(request.getCardType());
        card.setRarity(request.getRarity());
        card.setElementType(request.getElementType());
        card.setHp(request.getHp());
        card.setRetreatCost(request.getRetreatCost());
        card.setImageUrl(request.getImageUrl());

        Card updatedCard = cardRepository.save(card);
        return CardResponse.fromEntity(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));
        cardRepository.delete(card);
    }
}
