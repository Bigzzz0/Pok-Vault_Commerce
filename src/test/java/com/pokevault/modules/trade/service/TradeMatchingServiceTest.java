package com.pokevault.modules.trade.service;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.GameAccountRepository;
import com.pokevault.repository.OrderItemRepository;
import com.pokevault.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("In-Game Trade Matching Service: Auto-Match & Account Assignment Test")
class TradeMatchingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CardInventoryRepository cardInventoryRepository;

    @Mock
    private GameAccountRepository gameAccountRepository;

    @InjectMocks
    private TradeMatchingServiceImpl tradeMatchingService;

    private Card charizardCard;
    private GameAccount readyAccount1;
    private GameAccount readyAccount2;
    private GameAccount busyAccount;
    private CardInventory readyInventory1;
    private CardInventory readyInventory2;
    private CardInventory busyInventory;
    private Order sampleOrder;
    private OrderItem sampleItem;

    @BeforeEach
    void setUp() {
        charizardCard = Card.builder()
                .id(1L)
                .name("Charizard ex")
                .cardNumber("006/165")
                .build();

        readyAccount1 = GameAccount.builder()
                .id(10L)
                .accountCode("ACC-READY-01")
                .inGameName("Red_Trader")
                .friendId("1111-2222-3333")
                .tradeStatus(AccountTradeStatus.READY)
                .build();

        readyAccount2 = GameAccount.builder()
                .id(20L)
                .accountCode("ACC-READY-02")
                .inGameName("Blue_Trader")
                .friendId("4444-5555-6666")
                .tradeStatus(AccountTradeStatus.READY)
                .build();

        busyAccount = GameAccount.builder()
                .id(30L)
                .accountCode("ACC-BUSY-01")
                .inGameName("Green_Trader")
                .friendId("7777-8888-9999")
                .tradeStatus(AccountTradeStatus.BUSY)
                .build();

        // readyAccount1 มี 5 ใบ, readyAccount2 มี 2 ใบ, busyAccount มี 10 ใบ
        readyInventory1 = CardInventory.builder()
                .id(101L)
                .card(charizardCard)
                .gameAccount(readyAccount1)
                .quantity(5)
                .build();

        readyInventory2 = CardInventory.builder()
                .id(102L)
                .card(charizardCard)
                .gameAccount(readyAccount2)
                .quantity(2)
                .build();

        busyInventory = CardInventory.builder()
                .id(103L)
                .card(charizardCard)
                .gameAccount(busyAccount)
                .quantity(10)
                .build();

        sampleItem = OrderItem.builder()
                .id(501L)
                .inventory(readyInventory1)
                .quantity(2)
                .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                .unitPrice(new BigDecimal("500.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();

        sampleOrder = Order.builder()
                .id(1001L)
                .orderCode("ORD-2026-TEST")
                .customerFriendId("9999-0000-1111")
                .customerInGameName("AshKetchum")
                .items(new ArrayList<>(List.of(sampleItem)))
                .build();

        sampleItem.setOrder(sampleOrder);
    }

    @Nested
    @DisplayName("1. Recommendation Query Tests (getRecommendations)")
    class RecommendationQueryTests {

        @Test
        @DisplayName("getRecommendations should rank READY account with highest stock as #1 Best Candidate")
        void testGetRecommendationsRanksBestReadyAccount() {
            when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
            when(cardInventoryRepository.findByCardId(1L))
                    .thenReturn(List.of(busyInventory, readyInventory2, readyInventory1));

            List<TradeRecommendationResponse> recommendations = tradeMatchingService.getRecommendations(1001L);

            assertThat(recommendations).hasSize(1);
            TradeRecommendationResponse rec = recommendations.get(0);

            assertThat(rec.getOrderId()).isEqualTo(1001L);
            assertThat(rec.getOrderItemId()).isEqualTo(501L);
            assertThat(rec.getCardName()).isEqualTo("Charizard ex");
            assertThat(rec.getMatchFound()).isTrue();
            // ต้องเลือก readyAccount1 เพราะเป็น READY และมี 5 ใบ (มากกว่า readyAccount2 ที่มี 2 ใบ)
            assertThat(rec.getRecommendedAccountId()).isEqualTo(10L);
            assertThat(rec.getRecommendedAccountCode()).isEqualTo("ACC-READY-01");
            assertThat(rec.getAvailableStock()).isEqualTo(5);

            // ทางเลือกสำรอง (Alternatives)
            assertThat(rec.getAlternativeCandidates()).hasSize(2);
        }

        @Test
        @DisplayName("getRecommendationForItem should return recommendation for single item")
        void testGetRecommendationForItem() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(cardInventoryRepository.findByCardId(1L))
                    .thenReturn(List.of(readyInventory1));

            TradeRecommendationResponse response = tradeMatchingService.getRecommendationForItem(501L);

            assertThat(response).isNotNull();
            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("getRecommendations should return matchFound=false when no inventories exist")
        void testGetRecommendationsNoInventory() {
            when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of());

            List<TradeRecommendationResponse> recommendations = tradeMatchingService.getRecommendations(1001L);

            assertThat(recommendations).hasSize(1);
            TradeRecommendationResponse rec = recommendations.get(0);
            assertThat(rec.getMatchFound()).isFalse();
            assertThat(rec.getRecommendationReason()).contains("No active game accounts");
        }

        @Test
        @DisplayName("getRecommendations should return matchFound=false when only non-READY accounts exist")
        void testGetRecommendationsOnlyBusyAccount() {
            when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(busyInventory));

            List<TradeRecommendationResponse> recommendations = tradeMatchingService.getRecommendations(1001L);

            assertThat(recommendations).hasSize(1);
            TradeRecommendationResponse rec = recommendations.get(0);
            assertThat(rec.getMatchFound()).isFalse();
            assertThat(rec.getRecommendationReason()).contains("not READY");
        }
    }

    @Nested
    @DisplayName("2. Auto-Match Assignment Algorithm Tests (autoMatchOrderItem)")
    class AutoMatchAlgorithmTests {

        @Test
        @DisplayName("autoMatchOrderItem should assign best READY account, set FRIEND_PENDING, and save item")
        void testAutoMatchOrderItemSuccess() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(cardInventoryRepository.findByCardId(1L))
                    .thenReturn(List.of(readyInventory2, readyInventory1, busyInventory));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(501L);

            assertThat(response).isNotNull();
            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(10L);

            // ตรวจสอบการอัปเดตบน OrderItem entity
            assertThat(sampleItem.getAssignedAccount()).isEqualTo(readyAccount1);
            assertThat(sampleItem.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);

            verify(orderItemRepository, times(1)).save(sampleItem);
        }

        @Test
        @DisplayName("autoMatchOrderItem should throw InsufficientStockException when stock in READY accounts is inadequate")
        void testAutoMatchOrderItemInsufficientStock() {
            // ขอซื้อ 10 ใบ แต่ readyAccount1 มี 5 ใบ, readyAccount2 มี 2 ใบ
            sampleItem.setQuantity(10);

            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(cardInventoryRepository.findByCardId(1L))
                    .thenReturn(List.of(readyInventory1, readyInventory2));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("No READY game account found with sufficient stock (10 cards)");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("autoMatchOrderItem should throw InsufficientStockException when only BUSY accounts hold sufficient stock")
        void testAutoMatchOrderItemNoReadyAccounts() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(busyInventory));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("No READY game account found with sufficient stock");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("autoMatchOrderItem should throw InvalidOrderStateException when trade status is TRADE_SENT or COMPLETED")
        void testAutoMatchOrderItemGuardAgainstReassignment() {
            sampleItem.setTradeStatus(TradeFulfillmentStatus.TRADE_SENT);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot reassign account for OrderItem in status: TRADE_SENT");

            sampleItem.setTradeStatus(TradeFulfillmentStatus.COMPLETED);
            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot reassign account for OrderItem in status: COMPLETED");

            verify(orderItemRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("3. Batch Order Auto-Match Tests (autoMatchOrder)")
    class BatchAutoMatchTests {

        @Test
        @DisplayName("autoMatchOrder should iterate and auto-match all items in order")
        void testAutoMatchOrderSuccess() {
            when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(readyInventory1));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            List<TradeRecommendationResponse> responses = tradeMatchingService.autoMatchOrder(1001L);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getMatchFound()).isTrue();
            assertThat(sampleItem.getAssignedAccount()).isEqualTo(readyAccount1);
            assertThat(sampleItem.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);

            verify(orderItemRepository, times(1)).save(sampleItem);
        }
    }

    @Nested
    @DisplayName("4. Manual Account Assignment Tests (assignAccountToOrderItem)")
    class ManualAssignmentTests {

        @Test
        @DisplayName("assignAccountToOrderItem should manually assign READY account and set FRIEND_PENDING")
        void testManualAssignAccountSuccess() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(gameAccountRepository.findById(20L)).thenReturn(Optional.of(readyAccount2));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(readyInventory2));

            TradeRecommendationResponse response = tradeMatchingService.assignAccountToOrderItem(501L, 20L);

            assertThat(response).isNotNull();
            assertThat(sampleItem.getAssignedAccount()).isEqualTo(readyAccount2);
            assertThat(sampleItem.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);

            verify(orderItemRepository, times(1)).save(sampleItem);
        }

        @Test
        @DisplayName("assignAccountToOrderItem should reject assigning account if account trade status is not READY")
        void testManualAssignAccountNotReady() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(gameAccountRepository.findById(30L)).thenReturn(Optional.of(busyAccount));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 30L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot assign account ACC-BUSY-01 because its status is: BUSY");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should reject re-assignment when status is COMPLETED")
        void testManualAssignRejectWhenCompleted() {
            sampleItem.setTradeStatus(TradeFulfillmentStatus.COMPLETED);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot reassign account for OrderItem in status: COMPLETED");

            verify(gameAccountRepository, never()).findById(any());
            verify(orderItemRepository, never()).save(any());
        }
    }
}
