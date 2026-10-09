package com.pokevault.modules.trade.dto;

import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TradeRecommendationResponse: DTO & Builder Unit Tests")
class TradeRecommendationResponseTest {

    @Test
    @DisplayName("Builder and Getters should correctly set and retrieve fields")
    void testBuilderAndGetters() {
        TradeRecommendationResponse.CandidateAccountResponse candidate =
                TradeRecommendationResponse.CandidateAccountResponse.builder()
                        .accountId(20L)
                        .accountCode("ACC-READY-02")
                        .inGameName("Blue_Trader")
                        .friendId("4444-5555-6666")
                        .tradeStatus(AccountTradeStatus.READY)
                        .availableStock(3)
                        .build();

        TradeRecommendationResponse response = TradeRecommendationResponse.builder()
                .orderId(101L)
                .orderCode("ORD-2026-TEST")
                .orderItemId(501L)
                .cardId(1L)
                .cardName("Charizard ex")
                .cardNumber("006/165")
                .requestedQuantity(1)
                .fulfillmentStatus(TradeFulfillmentStatus.UNASSIGNED)
                .currentAssignedAccountId(null)
                .recommendedAccountId(10L)
                .recommendedAccountCode("ACC-READY-01")
                .recommendedInGameName("Red_Trader")
                .recommendedFriendId("1111-2222-3333")
                .accountStatus(AccountTradeStatus.READY)
                .availableStock(5)
                .matchFound(true)
                .recommendationReason("Recommended: Account ACC-READY-01 has 5 copies in stock.")
                .alternativeCandidates(List.of(candidate))
                .build();

        assertThat(response.getOrderId()).isEqualTo(101L);
        assertThat(response.getOrderCode()).isEqualTo("ORD-2026-TEST");
        assertThat(response.getOrderItemId()).isEqualTo(501L);
        assertThat(response.getCardId()).isEqualTo(1L);
        assertThat(response.getCardName()).isEqualTo("Charizard ex");
        assertThat(response.getCardNumber()).isEqualTo("006/165");
        assertThat(response.getRequestedQuantity()).isEqualTo(1);
        assertThat(response.getFulfillmentStatus()).isEqualTo(TradeFulfillmentStatus.UNASSIGNED);
        assertThat(response.getCurrentAssignedAccountId()).isNull();
        assertThat(response.getRecommendedAccountId()).isEqualTo(10L);
        assertThat(response.getRecommendedAccountCode()).isEqualTo("ACC-READY-01");
        assertThat(response.getRecommendedInGameName()).isEqualTo("Red_Trader");
        assertThat(response.getRecommendedFriendId()).isEqualTo("1111-2222-3333");
        assertThat(response.getAccountStatus()).isEqualTo(AccountTradeStatus.READY);
        assertThat(response.getAvailableStock()).isEqualTo(5);
        assertThat(response.getMatchFound()).isTrue();
        assertThat(response.getRecommendationReason()).contains("5 copies");
        assertThat(response.getAlternativeCandidates()).hasSize(1);

        assertThat(candidate.getAccountId()).isEqualTo(20L);
        assertThat(candidate.getAccountCode()).isEqualTo("ACC-READY-02");
        assertThat(candidate.getInGameName()).isEqualTo("Blue_Trader");
        assertThat(candidate.getFriendId()).isEqualTo("4444-5555-6666");
        assertThat(candidate.getTradeStatus()).isEqualTo(AccountTradeStatus.READY);
        assertThat(candidate.getAvailableStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("Default alternativeCandidates should be non-null and empty list")
    void testDefaultAlternativeCandidates() {
        TradeRecommendationResponse response = new TradeRecommendationResponse();
        assertThat(response.getAlternativeCandidates()).isNotNull().isEmpty();

        TradeRecommendationResponse builtResponse = TradeRecommendationResponse.builder().build();
        assertThat(builtResponse.getAlternativeCandidates()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Setters should update state appropriately")
    void testSetters() {
        TradeRecommendationResponse response = new TradeRecommendationResponse();
        response.setFulfillmentStatus(TradeFulfillmentStatus.FRIEND_PENDING);
        response.setCurrentAssignedAccountId(10L);
        response.setMatchFound(true);

        assertThat(response.getFulfillmentStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);
        assertThat(response.getCurrentAssignedAccountId()).isEqualTo(10L);
        assertThat(response.getMatchFound()).isTrue();
    }
}
