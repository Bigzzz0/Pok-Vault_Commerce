package com.pokevault.modules.trade.service;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.OrderStatus;
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
                .tradeStatus(AccountTradeStatus.BUSY_TRADING)
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

        @Test
        @DisplayName("autoMatchOrderItem should throw TradeStateConflictException when order is CANCELLED (จับคู่หลังยกเลิก)")
        void testAutoMatchOrderItemRejectedWhenOrderCancelled() {
            sampleOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(TradeStateConflictException.class)
                    .hasMessageContaining("Cannot auto-match account for order in terminal status: CANCELLED");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("autoMatchOrderItem should throw TradeStateConflictException when order is COMPLETED")
        void testAutoMatchOrderItemRejectedWhenOrderCompleted() {
            sampleOrder.setOrderStatus(OrderStatus.COMPLETED);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(501L))
                    .isInstanceOf(TradeStateConflictException.class)
                    .hasMessageContaining("Cannot auto-match account for order in terminal status: COMPLETED");

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

        @Test
        @DisplayName("autoMatchOrder should throw TradeStateConflictException when order is CANCELLED (จับคู่หลังยกเลิก)")
        void testAutoMatchOrderRejectedWhenOrderCancelled() {
            sampleOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrder(1001L))
                    .isInstanceOf(TradeStateConflictException.class)
                    .hasMessageContaining("Cannot auto-match accounts for order in terminal status: CANCELLED");
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

        @Test
        @DisplayName("assignAccountToOrderItem should reject re-assignment when status is TRADE_SENT")
        void testManualAssignRejectWhenTradeSent() {
            sampleItem.setTradeStatus(TradeFulfillmentStatus.TRADE_SENT);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot reassign account for OrderItem in status: TRADE_SENT");

            verify(gameAccountRepository, never()).findById(any());
            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should throw TradeStateConflictException when order is CANCELLED (จับคู่หลังยกเลิก)")
        void testManualAssignRejectedWhenOrderCancelled() {
            sampleOrder.setOrderStatus(OrderStatus.CANCELLED);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(TradeStateConflictException.class)
                    .hasMessageContaining("Cannot assign account for order in terminal status: CANCELLED");

            verify(gameAccountRepository, never()).findById(any());
            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should throw TradeStateConflictException when order is COMPLETED")
        void testManualAssignRejectedWhenOrderCompleted() {
            sampleOrder.setOrderStatus(OrderStatus.COMPLETED);
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(TradeStateConflictException.class)
                    .hasMessageContaining("Cannot assign account for order in terminal status: COMPLETED");

            verify(gameAccountRepository, never()).findById(any());
            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should reject when target account does not hold the card (เลือกบัญชีที่ไม่มีการ์ด)")
        void testManualAssignRejectedWhenAccountDoesNotHoldCard() {
            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(gameAccountRepository.findById(20L)).thenReturn(Optional.of(readyAccount2));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(readyInventory1));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Selected account ACC-READY-02 does not hold card: Charizard ex");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should reject when target account has insufficient stock (สต็อกไม่พอ)")
        void testManualAssignRejectedWhenTargetAccountHasInsufficientStock() {
            sampleItem.setQuantity(5);
            readyInventory2.setQuantity(2);

            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(gameAccountRepository.findById(20L)).thenReturn(Optional.of(readyAccount2));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(readyInventory2));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(501L, 20L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Selected account ACC-READY-02 has insufficient stock for card: Charizard ex (Available: 2, Requested: 5)");

            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("assignAccountToOrderItem should succeed and deduct stock to 0 when booking the last card (จองใบสุดท้าย)")
        void testManualAssignLastAvailableCardSuccess() {
            sampleItem.setQuantity(2);
            readyInventory1.setQuantity(5);
            readyInventory2.setQuantity(2);

            when(orderItemRepository.findById(501L)).thenReturn(Optional.of(sampleItem));
            when(gameAccountRepository.findById(20L)).thenReturn(Optional.of(readyAccount2));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(readyInventory2));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TradeRecommendationResponse response = tradeMatchingService.assignAccountToOrderItem(501L, 20L);

            assertThat(response).isNotNull();
            assertThat(sampleItem.getAssignedAccount()).isEqualTo(readyAccount2);
            assertThat(sampleItem.getTradeStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);
            assertThat(sampleItem.getInventory()).isEqualTo(readyInventory2);

            // จองใบสุดท้าย: คลังเป้าหมายลดเหลือ 0
            assertThat(readyInventory2.getQuantity()).isEqualTo(0);
            // คืนสต็อกให้คลังเดิม: 5 + 2 = 7
            assertThat(readyInventory1.getQuantity()).isEqualTo(7);

            verify(cardInventoryRepository, times(1)).save(readyInventory1);
            verify(cardInventoryRepository, times(1)).save(readyInventory2);
            verify(orderItemRepository, times(1)).save(sampleItem);
        }
    }

    @Nested
    @DisplayName("5. Card Condition & Stock Isolation Tests (Strict Condition Matching)")
    class CardConditionMatchingTests {

        private GameAccount accountA;
        private GameAccount accountB;
        private CardInventory invA_Mint;
        private CardInventory invA_Played;
        private CardInventory invB_Played;
        private OrderItem mintItem;

        @BeforeEach
        void initConditionData() {
            accountA = GameAccount.builder()
                    .id(100L)
                    .accountCode("ACC-MINT-01")
                    .inGameName("MintDealer")
                    .friendId("1111-1111-1111")
                    .tradeStatus(AccountTradeStatus.READY)
                    .build();

            accountB = GameAccount.builder()
                    .id(200L)
                    .accountCode("ACC-PLAYED-02")
                    .inGameName("PlayedDealer")
                    .friendId("2222-2222-2222")
                    .tradeStatus(AccountTradeStatus.READY)
                    .build();

            invA_Mint = CardInventory.builder()
                    .id(301L)
                    .card(charizardCard)
                    .gameAccount(accountA)
                    .condition(CardCondition.MINT)
                    .quantity(2)
                    .build();

            invA_Played = CardInventory.builder()
                    .id(302L)
                    .card(charizardCard)
                    .gameAccount(accountA)
                    .condition(CardCondition.PLAYED)
                    .quantity(5)
                    .build();

            invB_Played = CardInventory.builder()
                    .id(401L)
                    .card(charizardCard)
                    .gameAccount(accountB)
                    .condition(CardCondition.PLAYED)
                    .quantity(10)
                    .build();

            mintItem = OrderItem.builder()
                    .id(601L)
                    .inventory(invA_Mint)
                    .quantity(1)
                    .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                    .unitPrice(new BigDecimal("1000.00"))
                    .subtotal(new BigDecimal("1000.00"))
                    .order(sampleOrder)
                    .build();
        }

        @Test
        @DisplayName("Auto-match: Selects MINT account over PLAYED account even if PLAYED has much higher stock")
        void testAutoMatchSelectsMintOverPlayedEvenWithLowerStock() {
            // Customer booked 1 MINT from Account A (has 2). Account B has 10 PLAYED.
            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invB_Played, invA_Mint));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(601L);

            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(100L); // accountA
            assertThat(mintItem.getAssignedAccount()).isEqualTo(accountA);
            assertThat(mintItem.getInventory().getCondition()).isEqualTo(CardCondition.MINT);
        }

        @Test
        @DisplayName("Account with multiple conditions: Auto-match and stock counting must target exact condition inventory")
        void testSameAccountMultipleConditionsTargetsCorrectInventory() {
            // Account A has both MINT (inv 301, qty 0 unreserved) and PLAYED (inv 302, qty 5).
            // Item booked the last MINT (qty 0 unreserved).
            invA_Mint.setQuantity(0);
            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invA_Played, invA_Mint));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(601L);

            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(100L);
            assertThat(mintItem.getInventory().getId()).isEqualTo(301L); // must be invA_Mint, NOT invA_Played (302L)
            assertThat(mintItem.getInventory().getCondition()).isEqualTo(CardCondition.MINT);
        }

        @Test
        @DisplayName("Auto-match: Throws InsufficientStockException when condition matches but quantity is inadequate")
        void testConditionMatchesButQuantityInsufficient() {
            // Item was initially reserved from another source (id 999L) with MINT condition
            CardInventory tempInv = CardInventory.builder()
                    .id(999L)
                    .card(charizardCard)
                    .condition(CardCondition.MINT)
                    .quantity(0)
                    .build();
            mintItem.setInventory(tempInv);
            mintItem.setQuantity(3);
            invA_Mint.setQuantity(2); // Only 2 MINT available in Account A, but needs 3

            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            // Account B has 10 PLAYED, but only 2 MINT exists in Account A
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invA_Mint, invB_Played));

            assertThatThrownBy(() -> tradeMatchingService.autoMatchOrderItem(601L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("No READY game account found with sufficient stock (3 cards) for: Charizard ex in condition: MINT");
        }

        @Test
        @DisplayName("Last card booked: Warehouse unreserved stock is 0, but original inventory can still fulfill item")
        void testLastCardBookedStockZeroOriginalInventoryCanStillFulfill() {
            mintItem.setQuantity(1);
            invA_Mint.setQuantity(0); // Warehouse unreserved stock is 0 because order reserved it

            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invA_Mint));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TradeRecommendationResponse response = tradeMatchingService.autoMatchOrderItem(601L);

            assertThat(response.getMatchFound()).isTrue();
            assertThat(response.getRecommendedAccountId()).isEqualTo(100L);
            assertThat(mintItem.getAssignedAccount()).isEqualTo(accountA);
            assertThat(response.getFulfillmentStatus()).isEqualTo(TradeFulfillmentStatus.FRIEND_PENDING);
        }

        @Test
        @DisplayName("Manual assignment and recommendations enforce identical condition and stock rules")
        void testManualAssignmentAndRecommendationsEnforceSameRules() {
            // Customer requested MINT. Account B only has PLAYED (qty 10).
            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invB_Played));

            // 1. Recommendation must NOT recommend Account B
            TradeRecommendationResponse rec = tradeMatchingService.getRecommendationForItem(601L);
            assertThat(rec.getMatchFound()).isFalse();
            assertThat(rec.getAlternativeCandidates()).isEmpty();

            // 2. Manual assignment to Account B must be rejected
            when(gameAccountRepository.findById(200L)).thenReturn(Optional.of(accountB));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(601L, 200L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("does not hold card: Charizard ex in condition: MINT");
        }

        @Test
        @DisplayName("Manual assignment: Failure rolls back and does not alter stock or assignments")
        void testManualAssignFailureRollbackDoesNotAlterStock() {
            // Account C exists and is READY, but has 0 MINT stock (needs 1)
            GameAccount accountC = GameAccount.builder()
                    .id(300L)
                    .accountCode("ACC-MINT-03")
                    .tradeStatus(AccountTradeStatus.READY)
                    .build();

            CardInventory invC_Mint = CardInventory.builder()
                    .id(501L)
                    .card(charizardCard)
                    .gameAccount(accountC)
                    .condition(CardCondition.MINT)
                    .quantity(0) // insufficient
                    .build();

            int originalInvAStock = invA_Mint.getQuantity();

            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(gameAccountRepository.findById(300L)).thenReturn(Optional.of(accountC));
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invA_Mint, invC_Mint));

            assertThatThrownBy(() -> tradeMatchingService.assignAccountToOrderItem(601L, 300L))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("has insufficient stock for card: Charizard ex");

            // Stock of original inventory must not be changed
            assertThat(invA_Mint.getQuantity()).isEqualTo(originalInvAStock);
            assertThat(invC_Mint.getQuantity()).isEqualTo(0);
            assertThat(mintItem.getInventory()).isEqualTo(invA_Mint);
            assertThat(mintItem.getAssignedAccount()).isNull();

            // Verify no saves occurred
            verify(cardInventoryRepository, never()).save(any());
            verify(orderItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Manual assignment: Account with multiple conditions selects exact matching condition, not findFirst")
        void testManualAssignPicksMatchingConditionWhenAccountHasMultiple() {
            // Account A has invA_Mint (qty 2) and invA_Played (qty 5)
            // Item currently has invB_Mint (from Account B)
            CardInventory invB_Mint = CardInventory.builder()
                    .id(999L)
                    .card(charizardCard)
                    .gameAccount(accountB)
                    .condition(CardCondition.MINT)
                    .quantity(1)
                    .build();
            mintItem.setInventory(invB_Mint);

            when(orderItemRepository.findById(601L)).thenReturn(Optional.of(mintItem));
            when(gameAccountRepository.findById(100L)).thenReturn(Optional.of(accountA));
            // List returned has invA_Played FIRST, then invA_Mint
            when(cardInventoryRepository.findByCardId(1L)).thenReturn(List.of(invA_Played, invA_Mint));
            when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

            tradeMatchingService.assignAccountToOrderItem(601L, 100L);

            // Must pick invA_Mint (301L), NOT invA_Played (302L)
            assertThat(mintItem.getInventory().getId()).isEqualTo(301L);
            assertThat(mintItem.getInventory().getCondition()).isEqualTo(CardCondition.MINT);
            assertThat(mintItem.getAssignedAccount()).isEqualTo(accountA);
        }
    }
}
