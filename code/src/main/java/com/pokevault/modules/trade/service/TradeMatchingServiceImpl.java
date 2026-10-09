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

        // ตรวจสอบว่าออเดอร์ไม่ใช่ CANCELLED/COMPLETED
        Order order = item.getOrder();
        if (order != null && (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED)) {
            throw new TradeStateConflictException(
                    "Cannot auto-match account for order in terminal status: " + order.getOrderStatus());
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

        CardCondition targetCondition = (item.getInventory() != null) ? item.getInventory().getCondition() : null;
        CardInventory currentInv = item.getInventory();
        Long currentInvId = (currentInv != null) ? currentInv.getId() : null;

        // ค้นหาคลังทั้งหมดที่มีการ์ดใบนี้
        List<CardInventory> inventories = cardInventoryRepository.findByCardId(card.getId());

        // กรองเฉพาะไอดีที่มีสถานะ READY, มีสภาพการ์ดตรงกับที่สั่ง, และมีสต็อกเพียงพอกับจำนวนที่สั่ง
        // (หากเป็น Inventory ID เดิมที่ถือการจองของ OrderItem นี้อยู่แล้ว ให้นับสต็อกที่จองไว้กลับมารวมด้วย)
        CardInventory bestInventory = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null)
                .filter(inv -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY)
                .filter(inv -> targetCondition == null || inv.getCondition() == targetCondition)
                .filter(inv -> {
                    boolean isHolding = isHoldingCurrentReservation(item, inv, currentInvId);
                    int available = inv.getQuantity() != null ? inv.getQuantity() : 0;
                    int effectiveStock = isHolding ? available + item.getQuantity() : available;
                    return effectiveStock >= item.getQuantity();
                })
                .max(Comparator.comparing((CardInventory inv) -> {
                    boolean isHolding = isHoldingCurrentReservation(item, inv, currentInvId);
                    int available = inv.getQuantity() != null ? inv.getQuantity() : 0;
                    return isHolding ? available + item.getQuantity() : available;
                }))
                .orElseThrow(() -> new InsufficientStockException(
                        "No READY game account found with sufficient stock (" + item.getQuantity() + " cards) for: "
                                + card.getName() + (targetCondition != null ? " in condition: " + targetCondition : "")));

        GameAccount bestAccount = bestInventory.getGameAccount();

        // หากจับคู่ได้คลังใหม่ที่ไม่ใช่คลังเดิมที่จองไว้ ต้องย้ายการจองสต็อกจริง
        if (currentInv != null && !currentInv.getId().equals(bestInventory.getId())) {
            if (!bestInventory.hasSufficientStock(item.getQuantity())) {
                throw new InsufficientStockException(
                        "Selected account " + bestAccount.getAccountCode() + " has insufficient stock for: " + card.getName());
            }

            currentInv.restoreStock(item.getQuantity());
            cardInventoryRepository.save(currentInv);

            bestInventory.deductStock(item.getQuantity());
            cardInventoryRepository.save(bestInventory);

            item.setInventory(bestInventory);
        }

        // ดำเนินการมอบหมายไอดี และปรับสถานะเป็น FRIEND_PENDING
        item.setAssignedAccount(bestAccount);
        item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
        orderItemRepository.save(item);

        log.info("Successfully auto-matched account {} ({}) to OrderItem id: {} for card: {} (condition: {})",
                bestAccount.getAccountCode(), bestAccount.getInGameName(), item.getId(), card.getName(), targetCondition);

        return buildRecommendationForItem(item.getOrder(), item);
    }

    @Override
    @Transactional
    public List<TradeRecommendationResponse> autoMatchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new TradeStateConflictException(
                    "Cannot auto-match accounts for order in terminal status: " + order.getOrderStatus());
        }

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

        // 1. ตรวจสอบว่าออเดอร์ไม่ใช่ CANCELLED/COMPLETED
        Order order = item.getOrder();
        if (order != null && (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED)) {
            throw new TradeStateConflictException(
                    "Cannot assign account for order in terminal status: " + order.getOrderStatus());
        }

        // 2. ป้องกันการเปลี่ยนไอดีหากการเทรดเริ่มส่งมอบหรือเสร็จสิ้นไปแล้ว
        if (item.getTradeStatus() == TradeFulfillmentStatus.TRADE_SENT ||
            item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED) {
            throw new InvalidOrderStateException(
                    "Cannot reassign account for OrderItem in status: " + item.getTradeStatus());
        }

        // 3. ตรวจสอบบัญชีเกมที่ระบุ
        GameAccount account = gameAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", accountId));

        if (account.getTradeStatus() != AccountTradeStatus.READY) {
            throw new InvalidOrderStateException(
                    "Cannot assign account " + account.getAccountCode() + " because its status is: " + account.getTradeStatus());
        }

        CardInventory oldInventory = item.getInventory();
        if (oldInventory == null || oldInventory.getCard() == null) {
            throw new InvalidOrderStateException(
                    "Cannot match account: Card information not found for OrderItem id: " + orderItemId);
        }

        Card card = oldInventory.getCard();
        CardCondition targetCondition = oldInventory.getCondition();

        // 4. ตรวจสอบว่าบัญชีเป้าหมายถือการ์ดใบนี้หรือไม่
        List<CardInventory> inventories = cardInventoryRepository.findByCardId(card.getId());
        List<CardInventory> accountInventories = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null && inv.getGameAccount().getId().equals(accountId))
                .toList();

        if (accountInventories.isEmpty()) {
            throw new InsufficientStockException(
                    "Selected account " + account.getAccountCode() + " does not hold card: " + card.getName());
        }

        // ค้นหาคลังในบัญชีเป้าหมายที่มีสภาพการ์ดตรงกับรายการที่จอง
        CardInventory targetInventory = accountInventories.stream()
                .filter(inv -> targetCondition == null || inv.getCondition() == targetCondition)
                .findFirst()
                .orElseThrow(() -> new InsufficientStockException(
                        "Selected account " + account.getAccountCode() + " does not hold card: " + card.getName()
                                + (targetCondition != null ? " in condition: " + targetCondition : "")));

        // กรณีที่คลังเป้าหมายคือคลังเดิมที่จองไว้อยู่แล้ว (บัญชีเดิมและคลังเดิม)
        if (oldInventory.getId() != null && oldInventory.getId().equals(targetInventory.getId())) {
            item.setAssignedAccount(account);
            item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
            orderItemRepository.save(item);
        } else {
            // คลังใหม่: ตรวจสอบจำนวนให้เพียงพอก่อนย้ายการจองสต็อก
            if (!targetInventory.hasSufficientStock(item.getQuantity())) {
                throw new InsufficientStockException(
                        "Selected account " + account.getAccountCode() + " has insufficient stock for card: "
                                + card.getName() + " (Available: " + targetInventory.getQuantity()
                                + ", Requested: " + item.getQuantity() + ")");
            }

            // 5. สลับการจองสต็อกจริง: คืนสต็อกคลังเดิม และหักสต็อกคลังใหม่
            oldInventory.restoreStock(item.getQuantity());
            cardInventoryRepository.save(oldInventory);

            targetInventory.deductStock(item.getQuantity());
            cardInventoryRepository.save(targetInventory);

            item.setInventory(targetInventory);
            item.setAssignedAccount(account);
            item.setTradeStatus(TradeFulfillmentStatus.FRIEND_PENDING);
            orderItemRepository.save(item);
        }

        log.info("Manually assigned account {} ({}) to OrderItem id: {} for condition: {} and synchronized stock",
                account.getAccountCode(), account.getInGameName(), item.getId(), targetCondition);

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

        CardCondition targetCondition = (item.getInventory() != null) ? item.getInventory().getCondition() : null;
        Long currentInvId = (item.getInventory() != null) ? item.getInventory().getId() : null;

        // ค้นหาคลังทั้งหมดที่มีการ์ดใบนี้
        List<CardInventory> inventories = cardInventoryRepository.findByCardId(card.getId());

        // กรองเฉพาะคลังที่ผูกกับไอดีเกม, สภาพการ์ดตรงกับที่สั่ง, และมีสต็อกพร้อมส่งมอบเพียงพอ
        // (ใช้กฎเดียวกับการจับคู่จริง: หากเป็น Inventory ID เดิมที่จองไว้ ให้นับสต็อกที่จองกลับมารวมด้วย)
        List<CardInventory> availableInventories = inventories.stream()
                .filter(inv -> inv.getGameAccount() != null)
                .filter(inv -> targetCondition == null || inv.getCondition() == targetCondition)
                .filter(inv -> {
                    boolean isHolding = isHoldingCurrentReservation(item, inv, currentInvId);
                    int available = inv.getQuantity() != null ? inv.getQuantity() : 0;
                    int effectiveStock = isHolding ? available + item.getQuantity() : available;
                    return effectiveStock >= item.getQuantity();
                })
                .sorted(Comparator
                        .comparing((CardInventory inv) -> inv.getGameAccount().getTradeStatus() == AccountTradeStatus.READY ? 0 : 1)
                        .thenComparing(inv -> {
                            boolean isHolding = isHoldingCurrentReservation(item, inv, currentInvId);
                            int available = inv.getQuantity() != null ? inv.getQuantity() : 0;
                            return isHolding ? available + item.getQuantity() : available;
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

    private boolean isHoldingCurrentReservation(OrderItem item, CardInventory inv, Long currentInvId) {
        if (currentInvId == null || !currentInvId.equals(inv.getId())) {
            return false;
        }
        int available = inv.getQuantity() != null ? inv.getQuantity() : 0;
        return item.getAssignedAccount() != null || available == 0;
    }
}
