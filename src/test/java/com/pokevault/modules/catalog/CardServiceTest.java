package com.pokevault.modules.catalog;

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
import com.pokevault.modules.catalog.service.CardServiceImpl;
import com.pokevault.repository.CardExpansionRepository;
import com.pokevault.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardExpansionRepository cardExpansionRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardExpansion sampleExpansion;
    private Card pikachuCard;
    private Card charizardCard;

    @BeforeEach
    void setUp() {
        sampleExpansion = CardExpansion.builder()
                .id(1L)
                .code("A1")
                .name("Genetic Apex")
                .series("Genetic Apex")
                .releaseDate(LocalDate.of(2024, 10, 30))
                .totalCards(226)
                .build();

        pikachuCard = Card.builder()
                .id(1L)
                .expansion(sampleExpansion)
                .cardNumber("095/226")
                .name("Pikachu ex")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.DOUBLE_RARE)
                .elementType(ElementType.LIGHTNING)
                .hp(120)
                .retreatCost(1)
                .imageUrl("https://assets.pokevault.com/cards/a1_095.png")
                .build();

        charizardCard = Card.builder()
                .id(2L)
                .expansion(sampleExpansion)
                .cardNumber("036/226")
                .name("Charizard ex")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.IMMERSIVE_RARE)
                .elementType(ElementType.FIRE)
                .hp(180)
                .retreatCost(2)
                .imageUrl("https://assets.pokevault.com/cards/a1_036.png")
                .build();
    }

    @Test
    @DisplayName("Should return all cards without filter")
    void getAllCards_NoFilter_ReturnsAllCards() {
        when(cardRepository.findAll()).thenReturn(Arrays.asList(pikachuCard, charizardCard));

        List<CardResponse> results = cardService.getAllCards(null);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("Pikachu ex");
        assertThat(results.get(1).getName()).isEqualTo("Charizard ex");
        verify(cardRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should filter cards by element type and rarity")
    void getAllCards_WithFilter_ReturnsFilteredCards() {
        when(cardRepository.findAll()).thenReturn(Arrays.asList(pikachuCard, charizardCard));

        CardFilterRequest filter = CardFilterRequest.builder()
                .elementType("LIGHTNING")
                .rarity("DOUBLE_RARE")
                .build();

        List<CardResponse> results = cardService.getAllCards(filter);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Pikachu ex");
        assertThat(results.get(0).getElementType()).isEqualTo("LIGHTNING");
    }

    @Test
    @DisplayName("Should return card by ID when card exists")
    void getCardById_CardExists_ReturnsCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(pikachuCard));

        CardResponse result = cardService.getCardById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Pikachu ex");
        assertThat(result.getExpansionCode()).isEqualTo("A1");
        verify(cardRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when card does not exist")
    void getCardById_CardNotFound_ThrowsException() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should search cards by query string")
    void searchCards_ValidQuery_ReturnsMatchingCards() {
        when(cardRepository.findByNameContainingIgnoreCase("Char")).thenReturn(List.of(charizardCard));

        List<CardResponse> results = cardService.searchCards("Char");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Charizard ex");
        verify(cardRepository, times(1)).findByNameContainingIgnoreCase("Char");
    }

    @Test
    @DisplayName("Should return expansion by code")
    void getExpansionByCode_ValidCode_ReturnsExpansion() {
        when(cardExpansionRepository.findByCode("A1")).thenReturn(Optional.of(sampleExpansion));

        CardExpansionResponse result = cardService.getExpansionByCode("A1");

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("A1");
        assertThat(result.getName()).isEqualTo("Genetic Apex");
        verify(cardExpansionRepository, times(1)).findByCode("A1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when expansion code not found")
    void getExpansionByCode_NotFound_ThrowsException() {
        when(cardExpansionRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getExpansionByCode("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardExpansionRepository, times(1)).findByCode("UNKNOWN");
    }

    @Test
    @DisplayName("Should return paged cards with PageResponse metadata")
    void getCardsPaged_WithPagination_ReturnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Card> pagedCards = new PageImpl<>(List.of(pikachuCard, charizardCard), pageable, 2);

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagedCards);

        PageResponse<CardResponse> response = cardService.getCardsPaged(null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        verify(cardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should create card successfully when valid request is provided")
    void createCard_ValidRequest_ReturnsCardResponse() {
        CardRequest request = CardRequest.builder()
                .expansionId(1L)
                .cardNumber("095/226")
                .name("Pikachu ex")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.DOUBLE_RARE)
                .elementType(ElementType.LIGHTNING)
                .hp(120)
                .retreatCost(1)
                .imageUrl("https://assets.pokevault.com/cards/a1_095.png")
                .build();

        when(cardExpansionRepository.findById(1L)).thenReturn(Optional.of(sampleExpansion));
        when(cardRepository.save(any(Card.class))).thenReturn(pikachuCard);

        CardResponse result = cardService.createCard(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pikachu ex");
        assertThat(result.getExpansionCode()).isEqualTo("A1");
        verify(cardExpansionRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating card with non-existent expansion")
    void createCard_ExpansionNotFound_ThrowsException() {
        CardRequest request = CardRequest.builder()
                .expansionId(999L)
                .cardNumber("001/100")
                .name("Bulbasaur")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.COMMON)
                .build();

        when(cardExpansionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardExpansionRepository, times(1)).findById(999L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    @DisplayName("Should update existing card successfully")
    void updateCard_CardExists_ReturnsUpdatedCardResponse() {
        CardRequest request = CardRequest.builder()
                .expansionId(1L)
                .cardNumber("095/226")
                .name("Pikachu ex Updated")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.IMMERSIVE_RARE)
                .elementType(ElementType.LIGHTNING)
                .hp(130)
                .retreatCost(1)
                .build();

        Card updatedPikachu = Card.builder()
                .id(1L)
                .expansion(sampleExpansion)
                .cardNumber("095/226")
                .name("Pikachu ex Updated")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.IMMERSIVE_RARE)
                .elementType(ElementType.LIGHTNING)
                .hp(130)
                .retreatCost(1)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(pikachuCard));
        when(cardExpansionRepository.findById(1L)).thenReturn(Optional.of(sampleExpansion));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedPikachu);

        CardResponse result = cardService.updateCard(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Pikachu ex Updated");
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    @DisplayName("Should delete card when card exists")
    void deleteCard_CardExists_DeletesSuccessfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(pikachuCard));
        doNothing().when(cardRepository).delete(pikachuCard);

        cardService.deleteCard(1L);

        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).delete(pikachuCard);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent card")
    void deleteCard_CardNotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cardRepository, times(1)).findById(999L);
        verify(cardRepository, never()).delete(any(Card.class));
    }
}
