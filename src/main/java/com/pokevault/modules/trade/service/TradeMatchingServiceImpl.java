package com.pokevault.modules.trade.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse.CandidateAccountResponse;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.OrderItemRepository;
import com.pokevault.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeMatchingServiceImpl implements TradeMatchingService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CardInventoryRepository cardInventoryRepository;

    @Override
    public List<TradeRecommendationResponse> getRecommendations(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        log.info("Generating trade recommendations for order id: {}, code: {}", orderId, order.getOrderCode());

        return order.getItems().stream()
                .map(item -> buildRecommendationForItem(order, item))
                .toList();
    }

    @Override
    public TradeRecommendationResponse getRecommendationForItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

        return buildRecommendationForItem(item.getOrder(), item);
    }

    private TradeRecommendationResponse buildRecommendationForItem(Order order, OrderItem item) {
        Card card = (item.getInventory() != null) ? item.getInventory().getCard() : null;

        if (card == null) {
            return TradeRecommendationResponse.builder()
                    .orderId(order != null ? order.getId() : null)
                    .orderCode(order != null ? order.getOrderCode() : null)
                    .orderItemId(item.getId())
                    .requestedQuantity(item.getQuantity())
                    .fulfillmentStatus(item.getTradeStatus())
                    .matchFound(false)
                    .recommendationReason("Card information not found for this order item")
                    .alternativeCandidates(new ArrayList<>())
                    .build();
        }

        // ค้นหาคลังทั้งหมดที่มีการ์ดใบนี้
        List<CardInventory> inventories = cardInventoryRepository.findByCardId(card.getId());

        // กรองเฉพาะคลังที่ผูกกับไอดีเกม และมีสต็อกคงเหลือ > 0
        List<CardInventory> availableInventories = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null)
                .filter(inv -> inv.getQuantity() != null && inv.getQuantity() > 0)
                .sorted(Comparator
                        // ให้ความสำคัญกับสถานะ READY ก่อนสถานะอื่นๆ
                        .comparing(
                                (CardInventory inv) -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY
                                        ? 0
                                        : 1)
                        // จัดเรียงตามจำนวนสต็อกคงเหลือจากมากไปน้อย
                        .thenComparing(CardInventory::getQuantity, Comparator.reverseOrder()))
                .toList();

        TradeRecommendationResponse.TradeRecommendationResponseBuilder responseBuilder = TradeRecommendationResponse
                .builder()
                .orderId(order != null ? order.getId() : null)
                .orderCode(order != null ? order.getOrderCode() : null)
                .orderItemId(item.getId())
                .cardId(card.getId())
                .cardName(card.getName())
                .cardNumber(card.getCardNumber())
                .requestedQuantity(item.getQuantity())
                .fulfillmentStatus(item.getTradeStatus())
                .currentAssignedAccountId(item.getAssignedAccount() != null ? item.getAssignedAccount().getId() : null);

        if (availableInventories.isEmpty()) {
            return responseBuilder
                    .matchFound(false)
                    .recommendationReason("No active game accounts holding this card in stock")
                    .alternativeCandidates(new ArrayList<>())
                    .build();
        }

        // ค้นหา Best Match (ต้องเป็นสถานะ READY)
        CardInventory bestInventory = availableInventories.get(0);
        GameAccount bestAccount = bestInventory.getGameAccount();
        boolean isReady = bestAccount.getTradeStatus() == AccountTradeStatus.READY;

        if (isReady) {
            responseBuilder
                    .recommendedAccountId(bestAccount.getId())
                    .recommendedAccountCode(bestAccount.getAccountCode())
                    .recommendedInGameName(bestAccount.getInGameName())
                    .recommendedFriendId(bestAccount.getFriendId())
                    .accountStatus(bestAccount.getTradeStatus())
                    .availableStock(bestInventory.getQuantity())
                    .matchFound(true)
                    .recommendationReason("Found READY trade account with available stock ("
                            + bestInventory.getQuantity() + " cards)");
        } else {
            responseBuilder
                    .recommendedAccountId(bestAccount.getId())
                    .recommendedAccountCode(bestAccount.getAccountCode())
                    .recommendedInGameName(bestAccount.getInGameName())
                    .recommendedFriendId(bestAccount.getFriendId())
                    .accountStatus(bestAccount.getTradeStatus())
                    .availableStock(bestInventory.getQuantity())
                    .matchFound(false)
                    .recommendationReason(
                            "Account holds card but is currently " + bestAccount.getTradeStatus() + " (not READY)");
        }

        // ตัวเลือกสำรอง (Candidate Accounts อื่นๆ)
        List<CandidateAccountResponse> alternatives = availableInventories.stream()
                .filter(inv -> !inv.getGameAccount().getId().equals(bestAccount.getId()))
                .map(inv -> CandidateAccountResponse.builder()
                        .accountId(inv.getGameAccount().getId())
                        .accountCode(inv.getGameAccount().getAccountCode())
                        .inGameName(inv.getGameAccount().getInGameName())
                        .friendId(inv.getGameAccount().getFriendId())
                        .tradeStatus(inv.getGameAccount().getTradeStatus())
                        .availableStock(inv.getQuantity())
                        .build())
                .toList();

        responseBuilder.alternativeCandidates(alternatives);
        return responseBuilder.build();
    }
}
