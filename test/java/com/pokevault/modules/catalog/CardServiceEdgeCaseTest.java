package com.pokevault.modules.catalog;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.modules.catalog.dto.*;
import com.pokevault.modules.catalog.service.CardServiceImpl;
import com.pokevault.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceEdgeCaseTest {
    @Mock CardRepository cards;
    @Mock CardExpansionRepository expansions;
    @InjectMocks CardServiceImpl service;

    private Card card(String name, String code, ElementType element) {
        return Card.builder().id(1L).name(name).cardNumber("001/226")
                .expansion(CardExpansion.builder().code(code).name("Set " + code).build())
                .cardType(CardType.POKEMON).rarity(Rarity.COMMON).elementType(element).build();
    }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "\t"})
    void blankSearchReturnsAllCards(String query) {
        when(cards.findAll()).thenReturn(List.of(card("Pikachu", "A1", ElementType.LIGHTNING)));
        assertThat(service.searchCards(query)).extracting(CardResponse::getName).containsExactly("Pikachu");
        verify(cards, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test void searchTrimsQueryBeforeRepositoryCall() {
        when(cards.findByNameContainingIgnoreCase("Pika")).thenReturn(List.of());
        assertThat(service.searchCards("  Pika  ")).isEmpty();
        verify(cards).findByNameContainingIgnoreCase("Pika");
    }

    @Test void combinedFiltersAreCaseInsensitiveAndTrimmed() {
        when(cards.findAll()).thenReturn(List.of(card("Pikachu", "A1", ElementType.LIGHTNING),
                card("Charmander", "A1", ElementType.FIRE), card("Pikachu", "A2", ElementType.LIGHTNING)));
        var filter = CardFilterRequest.builder().name(" PIKA ").expansionCode(" a1 ")
                .rarity(" common ").cardType(" pokemon ").elementType(" lightning ").build();
        assertThat(service.getAllCards(filter)).extracting(CardResponse::getExpansionCode).containsExactly("A1");
    }

    @Test void filtersWithNoMatchReturnEmptyList() {
        when(cards.findAll()).thenReturn(List.of(card("Pikachu", "A1", ElementType.LIGHTNING)));
        assertThat(service.getAllCards(CardFilterRequest.builder().name("Mewtwo").build())).isEmpty();
    }

    @Test void expansionCardsMapToResponse() {
        when(cards.findByExpansionCode("A1")).thenReturn(List.of(card("Pikachu", "A1", ElementType.LIGHTNING)));
        assertThat(service.getCardsByExpansionCode("A1")).extracting(CardResponse::getName).containsExactly("Pikachu");
    }

    @Test void allExpansionsMapToResponses() {
        when(expansions.findAll()).thenReturn(List.of(CardExpansion.builder().code("A1").name("Genetic Apex").build()));
        assertThat(service.getAllExpansions()).extracting(CardExpansionResponse::getCode).containsExactly("A1");
    }

    @Test void emptyPagePreservesRequestedPageAndSort() {
        Pageable pageable = PageRequest.of(2, 5, Sort.by("name").descending());
        when(cards.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty(pageable));
        var result = service.getCardsPaged(null, pageable);
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isZero();
        verify(cards).findAll(any(Specification.class), eq(pageable));
    }

    @Test void createPersistsActualRequestFields() {
        var expansion = CardExpansion.builder().id(2L).code("A2").build();
        when(expansions.findById(2L)).thenReturn(Optional.of(expansion));
        when(cards.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var request = CardRequest.builder().expansionId(2L).name("Pikachu").cardNumber("002/100")
                .cardType(CardType.POKEMON).rarity(Rarity.COMMON).elementType(ElementType.LIGHTNING)
                .hp(60).retreatCost(1).imageUrl("https://example.test/card.png").build();
        service.createCard(request);
        var capture = ArgumentCaptor.forClass(Card.class);
        verify(cards).save(capture.capture());
        var saved = capture.getValue();
        assertThat(saved.getExpansion()).isSameAs(expansion);
        assertThat(saved.getName()).isEqualTo(request.getName());
        assertThat(saved.getCardNumber()).isEqualTo(request.getCardNumber());
        assertThat(saved.getCardType()).isEqualTo(request.getCardType());
        assertThat(saved.getRarity()).isEqualTo(request.getRarity());
        assertThat(saved.getElementType()).isEqualTo(request.getElementType());
        assertThat(saved.getHp()).isEqualTo(60);
        assertThat(saved.getRetreatCost()).isEqualTo(1);
        assertThat(saved.getImageUrl()).isEqualTo(request.getImageUrl());
    }

    @Test void updateMissingCardDoesNotWrite() {
        when(cards.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateCard(99L, CardRequest.builder().expansionId(1L).build()))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(expansions);
        verify(cards, never()).save(any());
    }

    @Test void updateMissingExpansionDoesNotMutateExistingCard() {
        Card existing = card("Pikachu", "A1", ElementType.LIGHTNING);
        when(cards.findById(1L)).thenReturn(Optional.of(existing));
        when(expansions.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateCard(1L, CardRequest.builder().expansionId(99L).name("Changed").build()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(existing.getName()).isEqualTo("Pikachu");
        verify(cards, never()).save(any());
    }
}
