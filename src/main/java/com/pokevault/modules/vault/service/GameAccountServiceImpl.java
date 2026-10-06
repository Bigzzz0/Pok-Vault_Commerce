package com.pokevault.modules.vault.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation สำหรับจัดการไอดีเกมของร้านค้าและคลังสต็อกการ์ด
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameAccountServiceImpl implements GameAccountService {

    private final GameAccountRepository gameAccountRepository;
    private final CardInventoryRepository cardInventoryRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional
    public GameAccountResponse createAccount(GameAccountRequest request) {
        log.info("Registering new game account vault: code={}, inGameName={}",
                request.getAccountCode(), request.getInGameName());

        if (gameAccountRepository.existsByAccountCode(request.getAccountCode())) {
            throw new IllegalArgumentException("Account code already exists: " + request.getAccountCode());
        }

        GameAccount account = GameAccount.builder()
                .accountCode(request.getAccountCode())
                .inGameName(request.getInGameName())
                .friendId(request.getFriendId())
                .tradeStatus(request.getTradeStatus() != null ? request.getTradeStatus() : AccountTradeStatus.READY)
                .buyInCost(request.getBuyInCost() != null ? request.getBuyInCost() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .build();

        GameAccount saved = gameAccountRepository.save(account);
        log.info("Successfully registered game account [ID: {}]: code={}", saved.getId(), saved.getAccountCode());
        return GameAccountResponse.fromEntity(saved, 0);
    }

    @Override
    public List<GameAccountResponse> getAllAccounts() {
        List<GameAccount> accounts = gameAccountRepository.findAll();
        return accounts.stream()
                .map(account -> {
                    int totalCards = cardInventoryRepository.findByGameAccountId(account.getId())
                            .stream()
                            .mapToInt(CardInventory::getQuantity)
                            .sum();
                    return GameAccountResponse.fromEntity(account, totalCards);
                })
                .toList();
    }

    @Override
    public GameAccountResponse getAccountById(Long id) {
        GameAccount account = gameAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", id));

        int totalCards = cardInventoryRepository.findByGameAccountId(id)
                .stream()
                .mapToInt(CardInventory::getQuantity)
                .sum();

        return GameAccountResponse.fromEntity(account, totalCards);
    }

    @Override
    @Transactional
    public AccountCardResponse addPulledCard(Long id, AddPulledCardRequest request) {
        log.info("Recording pack pull for game account [ID: {}]: cardId={}, qty={}",
                id, request.getCardId(), request.getQuantity());

        GameAccount account = gameAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", id));

        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", request.getCardId()));

        CardCondition condition = request.getCondition() != null ? request.getCondition() : CardCondition.MINT;
        int quantityToAdd = request.getQuantity() > 0 ? request.getQuantity() : 1;

        Optional<CardInventory> existingInventory = cardInventoryRepository
                .findByCardIdAndGameAccountIdAndCondition(card.getId(), account.getId(), condition);

        CardInventory inventoryToSave;
        if (existingInventory.isPresent()) {
            inventoryToSave = existingInventory.get();
            int previousQty = inventoryToSave.getQuantity();
            inventoryToSave.restoreStock(quantityToAdd);

            if (request.getSellingPrice() != null && request.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                inventoryToSave.setSellingPrice(request.getSellingPrice());
            }

            log.info(
                    "Updated existing card inventory [ID: {}] for account [{}]: card='{}' ({}), condition={}, added={}, newQty={}",
                    inventoryToSave.getId(), account.getAccountCode(), card.getName(), card.getCardNumber(),
                    condition, quantityToAdd, inventoryToSave.getQuantity());
        } else {
            inventoryToSave = CardInventory.builder()
                    .card(card)
                    .gameAccount(account)
                    .condition(condition)
                    .quantity(quantityToAdd)
                    .buyInPrice(request.getBuyInPrice() != null ? request.getBuyInPrice() : BigDecimal.ZERO)
                    .sellingPrice(request.getSellingPrice() != null ? request.getSellingPrice() : BigDecimal.ZERO)
                    .storageSlot(request.getStorageSlot())
                    .build();

            log.info("Created new card inventory for account [{}]: card='{}' ({}), condition={}, qty={}",
                    account.getAccountCode(), card.getName(), card.getCardNumber(), condition, quantityToAdd);
        }

        CardInventory saved = cardInventoryRepository.save(inventoryToSave);
        return AccountCardResponse.fromEntity(saved);
    }

    @Override
    public List<AccountCardResponse> getAccountCards(Long id) {
        if (!gameAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("GameAccount", "id", id);
        }

        List<CardInventory> inventories = cardInventoryRepository.findByGameAccountId(id);
        return inventories.stream()
                .map(AccountCardResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public GameAccountResponse updateTradeStatus(Long id, AccountTradeStatus status) {
        GameAccount account = gameAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GameAccount", "id", id));

        account.setTradeStatus(status);
        GameAccount updated = gameAccountRepository.save(account);
        log.info("Updated trade status for account [{}]: newStatus={}", updated.getAccountCode(), status);

        int totalCards = cardInventoryRepository.findByGameAccountId(id)
                .stream()
                .mapToInt(CardInventory::getQuantity)
                .sum();

        return GameAccountResponse.fromEntity(updated, totalCards);
    }

    @Override
    public BigDecimal calculateTotalVaultCostValue() {
        return cardInventoryRepository.calculateTotalVaultCostValue();
    }

    @Override
    public BigDecimal calculateTotalVaultSellingValue() {
        return cardInventoryRepository.calculateTotalVaultSellingValue();
    }
}
