package com.pokevault.modules.vault.service;

import com.pokevault.common.exception.ResourceNotFoundException;
import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.GameAccount;
import com.pokevault.domain.enums.AccountTradeStatus;
import com.pokevault.domain.enums.CardCondition;
import com.pokevault.domain.enums.CardType;
import com.pokevault.domain.enums.ElementType;
import com.pokevault.domain.enums.Rarity;
import com.pokevault.modules.vault.dto.AccountCardResponse;
import com.pokevault.modules.vault.dto.AddPulledCardRequest;
import com.pokevault.modules.vault.dto.GameAccountRequest;
import com.pokevault.modules.vault.dto.GameAccountResponse;
import com.pokevault.repository.CardInventoryRepository;
import com.pokevault.repository.CardRepository;
import com.pokevault.repository.GameAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test สำหรับ GameAccountServiceImpl
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@ExtendWith(MockitoExtension.class)
class GameAccountServiceTest {

    @Mock
    private GameAccountRepository gameAccountRepository;

    @Mock
    private CardInventoryRepository cardInventoryRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private GameAccountServiceImpl gameAccountService;

    private GameAccount sampleAccount;
    private Card sampleCard;
    private CardInventory sampleInventory;

    @BeforeEach
    void setUp() {
        sampleAccount = GameAccount.builder()
                .id(1L)
                .accountCode("VAULT_ACC_01")
                .inGameName("ApexMaster_01")
                .friendId("1234-5678-9012-3456")
                .tradeStatus(AccountTradeStatus.READY)
                .buyInCost(new BigDecimal("500.00"))
                .notes("Primary trading account")
                .build();

        sampleCard = Card.builder()
                .id(100L)
                .cardNumber("A1-001")
                .name("Mewtwo ex")
                .cardType(CardType.POKEMON)
                .rarity(Rarity.IMMERSIVE_RARE)
                .elementType(ElementType.PSYCHIC)
                .hp(150)
                .imageUrl("https://images.pokevault.com/mewtwo-ex.png")
                .build();

        sampleInventory = CardInventory.builder()
                .id(10L)
                .card(sampleCard)
                .gameAccount(sampleAccount)
                .condition(CardCondition.MINT)
                .quantity(2)
                .buyInPrice(new BigDecimal("50.00"))
                .sellingPrice(new BigDecimal("750.00"))
                .storageSlot("SLOT-A1")
                .build();
    }

    @Nested
    @DisplayName("Tests for addPulledCard (+ Add Pull)")
    class AddPulledCardTests {

        @Test
        @DisplayName("addPulledCard: เมื่อมีการ์ดในคลังอยู่แล้ว ควรอัปเดตสต็อกเพิ่มและบันทึก")
        void addPulledCard_WhenExistingInventory_ShouldIncrementStock() {
            // Arrange
            AddPulledCardRequest request = AddPulledCardRequest.builder()
                    .cardId(100L)
                    .quantity(3)
                    .condition(CardCondition.MINT)
                    .sellingPrice(new BigDecimal("800.00"))
                    .build();

            when(gameAccountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(cardRepository.findById(100L)).thenReturn(Optional.of(sampleCard));
            when(cardInventoryRepository.findByCardIdAndGameAccountIdAndCondition(100L, 1L, CardCondition.MINT))
                    .thenReturn(Optional.of(sampleInventory));
            when(cardInventoryRepository.save(any(CardInventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AccountCardResponse response = gameAccountService.addPulledCard(1L, request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getQuantity()).isEqualTo(5); // 2 + 3 = 5
            assertThat(response.getSellingPrice()).isEqualByComparingTo(new BigDecimal("800.00"));
            verify(cardInventoryRepository).save(sampleInventory);
        }

        @Test
        @DisplayName("addPulledCard: เมื่อเปิดได้การ์ดใหม่ที่ยังไม่มีในไอดี ควรสร้าง CardInventory ใหม่")
        void addPulledCard_WhenNewInventory_ShouldCreateNewRecord() {
            // Arrange
            AddPulledCardRequest request = AddPulledCardRequest.builder()
                    .cardId(100L)
                    .quantity(1)
                    .condition(CardCondition.MINT)
                    .buyInPrice(new BigDecimal("40.00"))
                    .sellingPrice(new BigDecimal("700.00"))
                    .storageSlot("SLOT-B2")
                    .build();

            when(gameAccountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(cardRepository.findById(100L)).thenReturn(Optional.of(sampleCard));
            when(cardInventoryRepository.findByCardIdAndGameAccountIdAndCondition(100L, 1L, CardCondition.MINT))
                    .thenReturn(Optional.empty());

            ArgumentCaptor<CardInventory> captor = ArgumentCaptor.forClass(CardInventory.class);
            when(cardInventoryRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            AccountCardResponse response = gameAccountService.addPulledCard(1L, request);

            // Assert
            assertThat(response).isNotNull();
            CardInventory savedInventory = captor.getValue();
            assertThat(savedInventory.getQuantity()).isEqualTo(1);
            assertThat(savedInventory.getCard().getName()).isEqualTo("Mewtwo ex");
            assertThat(savedInventory.getGameAccount().getAccountCode()).isEqualTo("VAULT_ACC_01");
            assertThat(savedInventory.getStorageSlot()).isEqualTo("SLOT-B2");
        }

        @Test
        @DisplayName("addPulledCard: เมื่อไม่พบบัญชีเกม ควรโยน ResourceNotFoundException")
        void addPulledCard_WhenAccountNotFound_ShouldThrowException() {
            AddPulledCardRequest request = AddPulledCardRequest.builder().cardId(100L).quantity(1).build();
            when(gameAccountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameAccountService.addPulledCard(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("GameAccount");
        }

        @Test
        @DisplayName("addPulledCard: เมื่อไม่พบข้อมูลการ์ด ควรโยน ResourceNotFoundException")
        void addPulledCard_WhenCardNotFound_ShouldThrowException() {
            AddPulledCardRequest request = AddPulledCardRequest.builder().cardId(999L).quantity(1).build();
            when(gameAccountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(cardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> gameAccountService.addPulledCard(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Card");
        }
    }

    @Nested
    @DisplayName("Tests for Account Management")
    class AccountManagementTests {

        @Test
        @DisplayName("createAccount: ลงทะเบียนไอดีเกมสำเร็จ")
        void createAccount_Success() {
            GameAccountRequest request = GameAccountRequest.builder()
                    .accountCode("NEW_ACC_02")
                    .inGameName("PikaTrainer")
                    .friendId("9999-8888-7777-6666")
                    .tradeStatus(AccountTradeStatus.READY)
                    .buyInCost(new BigDecimal("300.00"))
                    .build();

            when(gameAccountRepository.existsByAccountCode("NEW_ACC_02")).thenReturn(false);
            when(gameAccountRepository.save(any(GameAccount.class))).thenAnswer(invocation -> {
                GameAccount acc = invocation.getArgument(0);
                acc.setId(2L);
                return acc;
            });

            GameAccountResponse response = gameAccountService.createAccount(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccountCode()).isEqualTo("NEW_ACC_02");
            assertThat(response.getInGameName()).isEqualTo("PikaTrainer");
            verify(gameAccountRepository).save(any(GameAccount.class));
        }

        @Test
        @DisplayName("createAccount: เมื่อรหัส accountCode ซ้ำ ควรโยน IllegalArgumentException")
        void createAccount_WhenDuplicateCode_ShouldThrowException() {
            GameAccountRequest request = GameAccountRequest.builder().accountCode("VAULT_ACC_01").build();
            when(gameAccountRepository.existsByAccountCode("VAULT_ACC_01")).thenReturn(true);

            assertThatThrownBy(() -> gameAccountService.createAccount(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account code already exists");
        }

        @Test
        @DisplayName("getAccountById: ค้นหาไอดีสำเร็จพร้อมจำนวนการ์ดรวม")
        void getAccountById_Success() {
            when(gameAccountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(cardInventoryRepository.findByGameAccountId(1L)).thenReturn(List.of(sampleInventory));

            GameAccountResponse response = gameAccountService.getAccountById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTotalCards()).isEqualTo(2);
        }

        @Test
        @DisplayName("getAccountCards: ดึงรายการการ์ดในไอดีเกมสำเร็จ")
        void getAccountCards_Success() {
            when(gameAccountRepository.existsById(1L)).thenReturn(true);
            when(cardInventoryRepository.findByGameAccountId(1L)).thenReturn(List.of(sampleInventory));

            List<AccountCardResponse> cards = gameAccountService.getAccountCards(1L);

            assertThat(cards).hasSize(1);
            assertThat(cards.get(0).getCardName()).isEqualTo("Mewtwo ex");
            assertThat(cards.get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("updateTradeStatus: อัปเดตสถานะความพร้อมเทรดสำเร็จ")
        void updateTradeStatus_Success() {
            when(gameAccountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(gameAccountRepository.save(any(GameAccount.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(cardInventoryRepository.findByGameAccountId(1L)).thenReturn(List.of(sampleInventory));

            GameAccountResponse response = gameAccountService.updateTradeStatus(1L, AccountTradeStatus.BUSY_TRADING);

            assertThat(response).isNotNull();
            assertThat(response.getTradeStatus()).isEqualTo(AccountTradeStatus.BUSY_TRADING);
            assertThat(sampleAccount.getTradeStatus()).isEqualTo(AccountTradeStatus.BUSY_TRADING);
        }
    }
}
