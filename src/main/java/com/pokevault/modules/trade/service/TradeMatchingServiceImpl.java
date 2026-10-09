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
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse;
import com.pokevault.modules.trade.dto.TradeRecommendationResponse.CandidateAccountResponse;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.GameAccountRepository;
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
    private final GameAccountRepository gameAccountRepository;

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

    @Override
    @Transactional
    public TradeRecommendationResponse autoMatchOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

        if (item.getOrder() != null && (item.getOrder().getOrderStatus() == OrderStatus.CANCELLED ||
            item.getOrder().getOrderStatus() == OrderStatus.COMPLETED)) {
            throw new InvalidOrderStateException(
                    "Cannot match account for Order in terminal status: " + item.getOrder().getOrderStatus());
        }

        // ป้องกันการเปลี่ยนไอดีหากการเทรดเริ่มส่งมอบหรือเสร็จสิ้นไปแล้ว
        if (item.getTradeStatus() == TradeFulfillmentStatus.TRADE_SENT ||
            item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Cannot reassign account for OrderItem in status: " + item.getTradeStatus());
        }

        Card card = (item.getInventory() != null) ? item.getInventory().getCard() : null;
        if (card == null) {
            throw new InvalidOrderStateException(
                    "Cannot match account: Card information not found for OrderItem id: " + orderItemId);
        }

        // ค้นหาคลังทั้งหมดที่มีการ์ดใบนี้
        List<CardInventory> inventories = cardInventoryRepository.findByCardId(card.getId());

        // กรองเฉพาะไอดีที่มีสถานะ READY และมีสต็อกเพียงพอกับจำนวนที่สั่ง
        // (หากเป็นไอดีที่ถือการจองของ OrderItem นี้อยู่แล้ว ให้ถือว่ามีสต็อกพร้อมส่งมอบได้แม้สต็อกคงเหลือพร้อมขายเป็น 0)
        CardInventory bestInventory = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null)
                .filter(inv -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY)
                .filter(inv -> {
                    boolean isAlreadyHolding = item.getAssignedAccount() != null &&
                            inv.getGameAccount().getId().equals(item.getAssignedAccount().getId());
                    return isAlreadyHolding || (inv.getQuantity() != null && inv.getQuantity() >= item.getQuantity());
                })
                .max(Comparator.comparing((CardInventory inv) -> {
                    boolean isAlreadyHolding = item.getAssignedAccount() != null &&
                            inv.getGameAccount().getId().equals(item.getAssignedAccount().getId());
                    int qty = inv.getQuantity() != null ? inv.getQuantity() : 0;
                    return isAlreadyHolding ? qty + item.getQuantity() : qty;
                }))
                .orElseThrow(() -> new InsufficientStockException(
                        "No READY game account found with sufficient stock (" + item.getQuantity() + " cards) for: " + card.getName()));

        GameAccount bestAccount = bestInventory.getGameAccount();

        // หากจับคู่ได้คลังใหม่ที่ไม่ใช่คลังเดิมที่จองไว้ ต้องย้ายการจองสต็อกจริง
        if (item.getInventory() != null && !item.getInventory().getId().equals(bestInventory.getId())) {
            CardInventory oldInv = item.getInventory();
            oldInv.restoreStock(item.getQuantity());
            cardInventoryRepository.save(oldInv);

            bestInventory.deductStock(item.getQuantity());
            cardInventoryRepository.save(bestInventory);

            item.setInventory(bestInventory);
        }

        // ดำเนินการมอบหมายไอดี และปรับสถานะเป็น FRIEND_PENDING
        item.setAssignedAccount(bestAccount);
        item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
        orderItemRepository.save(item);

        log.info("Successfully auto-matched account {} ({}) to OrderItem id: {} for card: {}",
                bestAccount.getAccountCode(), bestAccount.getInGameName(), item.getId(), card.getName());

        return buildRecommendationForItem(item.getOrder(), item);
    }

    @Override
    @Transactional
    public List<TradeRecommendationResponse> autoMatchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        log.info("Auto-matching game accounts for order id: {}, code: {}", orderId, order.getOrderCode());

        List<TradeRecommendationResponse> responses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            responses.add(autoMatchOrderItem(item.getId()));
        }
        return responses;
    }

    @Override
    @Transactional
    public TradeRecommendationResponse assignAccountToOrderItem(Long orderItemId, Long accountId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", "id", orderItemId));

        if (item.getOrder() != null && (item.getOrder().getOrderStatus() == OrderStatus.CANCELLED ||
            item.getOrder().getOrderStatus() == OrderStatus.COMPLETED)) {
            throw new InvalidOrderStateException(
                    "Cannot assign account for Order in terminal status: " + item.getOrder().getOrderStatus());
        }

        if (item.getTradeStatus() == TradeFulfillmentStatus.TRADE_SENT ||
            item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Cannot reassign account for OrderItem in status: " + item.getTradeStatus());
        }

        GameAccount account = gameAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", accountId));

        if (account.getTradeStatus() != AccountTradeStatus.READY) {
            throw new InvalidOrderStateException(
                    "Cannot assign account " + account.getAccountCode() + " because its status is: " + account.getTradeStatus());
        }

        CardInventory oldInv = item.getInventory();
        if (oldInv != null && oldInv.getGameAccount() != null && oldInv.getGameAccount().getId().equals(accountId)) {
            // บัญชีเดิมที่ถือสต็อกที่จองไว้อยู่แล้ว
            item.setAssignedAccount(account);
            item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
            orderItemRepository.save(item);
        } else {
            // บัญชีใหม่ ต้องค้นหา CardInventory ของบัญชีนี้ที่มีการ์ดใบเดียวกัน
            Card card = (oldInv != null) ? oldInv.getCard() : null;
            if (card == null) {
                throw new InvalidOrderStateException(
                        "Cannot assign account: Card information not found for OrderItem id: " + orderItemId);
            }
            List<CardInventory> targetInventories = cardInventoryRepository.findByCardId(card.getId());
            CardInventory newInv = targetInventories.stream()
                    .filter(inv -> inv.getGameAccount() != null && inv.getGameAccount().getId().equals(accountId))
                    .findFirst()
                    .orElseThrow(() -> new InsufficientStockException(
                            "Account " + account.getAccountCode() + " does not hold card " + card.getName()));

            if (!newInv.hasSufficientStock(item.getQuantity())) {
                throw new InsufficientStockException(
                        "Account " + account.getAccountCode() + " has insufficient stock for: " + card.getName()
                                + " (Available: " + newInv.getQuantity() + ", Requested: " + item.getQuantity() + ")");
            }

            // ย้ายการจองสต็อกจริง: คืนคลังเก่า หักคลังใหม่
            if (oldInv != null) {
                oldInv.restoreStock(item.getQuantity());
                cardInventoryRepository.save(oldInv);
            }
            newInv.deductStock(item.getQuantity());
            cardInventoryRepository.save(newInv);

            item.setInventory(newInv);
            item.setAssignedAccount(account);
            item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
            orderItemRepository.save(item);
        }

        log.info("Manually assigned account {} ({}) to OrderItem id: {} and synchronized stock",
                account.getAccountCode(), account.getInGameName(), item.getId());

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

        // กรองเฉพาะคลังที่ผูกกับไอดีเกม และมีสต็อกคงเหลือ > 0 (หรือเป็นไอดีที่ถือการจองของ OrderItem นี้อยู่แล้ว)
        List<CardInventory> availableInventories = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null)
                .filter(inv -> {
                    boolean isAlreadyHolding = item.getAssignedAccount() != null &&
                            inv.getGameAccount().getId().equals(item.getAssignedAccount().getId());
                    return isAlreadyHolding || (inv.getQuantity() != null && inv.getQuantity() > 0);
                })
                .sorted(Comparator
                        .comparing((CardInventory inv) -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY ? 0 : 1)
                        .thenComparing(inv -> {
                            boolean isAlreadyHolding = item.getAssignedAccount() != null &&
                                    inv.getGameAccount().getId().equals(item.getAssignedAccount().getId());
                            int qty = inv.getQuantity() != null ? inv.getQuantity() : 0;
                            return isAlreadyHolding ? qty + item.getQuantity() : qty;
                        }, Comparator.reverseOrder()))
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

        // ค้นหา Best Match
        CardInventory bestInventory = availableInventories.get(0);
        GameAccount bestAccount = bestInventory.getGameAccount();
        boolean isReady = bestAccount.getTradeStatus() == AccountTradeStatus.READY;
        int displayStock = bestInventory.getQuantity() != null ? bestInventory.getQuantity() : 0;

        if (isReady) {
            responseBuilder
                    .recommendedAccountId(bestAccount.getId())
                    .recommendedAccountCode(bestAccount.getAccountCode())
                    .recommendedInGameName(bestAccount.getInGameName())
                    .recommendedFriendId(bestAccount.getFriendId())
                    .accountStatus(bestAccount.getTradeStatus())
                    .availableStock(displayStock)
                    .matchFound(true)
                    .recommendationReason("Found READY trade account with available stock ("
                            + displayStock + " cards)");
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
