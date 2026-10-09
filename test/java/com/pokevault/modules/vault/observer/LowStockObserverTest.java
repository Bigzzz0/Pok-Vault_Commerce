package com.pokevault.modules.vault.observer;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.repository.CardInventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit Test สำหรับ LowStockObserver (GoF Observer Pattern / Event Listener)
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@ExtendWith(MockitoExtension.class)
class LowStockObserverTest {

    @Mock
    private CardInventoryRepository cardInventoryRepository;

    @InjectMocks
    private LowStockObserver lowStockObserver;

    private Card mewtwoCard;
    private Card pikachuCard;
    private CardInventory mewtwoInventory;
    private CardInventory pikachuInventory;

    @BeforeEach
    void setUp() {
        mewtwoCard = Card.builder()
                .id(100L)
                .name("Mewtwo ex")
                .cardNumber("A1-001")
                .build();

        pikachuCard = Card.builder()
                .id(200L)
                .name("Pikachu")
                .cardNumber("A1-025")
                .build();

        mewtwoInventory = CardInventory.builder()
                .id(10L)
                .card(mewtwoCard)
                .quantity(1)
                .build();

        pikachuInventory = CardInventory.builder()
                .id(20L)
                .card(pikachuCard)
                .quantity(5)
                .build();
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อสต็อกเหลือน้อยกว่าเกณฑ์ (<= 2) ต้องตรวจสอบและแจ้งเตือนสต็อกต่ำ")
    void onOrderPlaced_WhenStockBelowThreshold_ShouldQueryStockAndWarn() {
        OrderItem item = OrderItem.builder()
                .id(1L)
                .inventory(mewtwoInventory)
                .quantity(1)
                .build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(1L)
                .orderCode("ORD-2026-001")
                .items(List.of(item))
                .build();

        when(cardInventoryRepository.sumQuantityByCardId(100L)).thenReturn(1);

        assertThatCode(() -> lowStockObserver.onOrderPlaced(event))
                .doesNotThrowAnyException();

        verify(cardInventoryRepository, times(1)).sumQuantityByCardId(100L);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อสต็อกเท่ากับเกณฑ์พอดี (== 2) ต้องตรวจสอบและแจ้งเตือนสต็อกต่ำ")
    void onOrderPlaced_WhenStockEqualsThreshold_ShouldTriggerWarning() {
        OrderItem item = OrderItem.builder()
                .id(1L)
                .inventory(mewtwoInventory)
                .quantity(1)
                .build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(2L)
                .orderCode("ORD-2026-002")
                .items(List.of(item))
                .build();

        when(cardInventoryRepository.sumQuantityByCardId(100L)).thenReturn(LowStockObserver.LOW_STOCK_THRESHOLD);

        lowStockObserver.onOrderPlaced(event);

        verify(cardInventoryRepository).sumQuantityByCardId(100L);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อสต็อกยังมีเพียงพอ (> 2) ต้องตรวจสอบและบันทึกข้อมูลปกติ")
    void onOrderPlaced_WhenStockAboveThreshold_ShouldLogNormal() {
        OrderItem item = OrderItem.builder()
                .id(2L)
                .inventory(pikachuInventory)
                .quantity(1)
                .build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(3L)
                .orderCode("ORD-2026-003")
                .items(List.of(item))
                .build();

        when(cardInventoryRepository.sumQuantityByCardId(200L)).thenReturn(5);

        lowStockObserver.onOrderPlaced(event);

        verify(cardInventoryRepository).sumQuantityByCardId(200L);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อออเดอร์มีสินค้าหลายรายการ ต้องตรวจสอบสต็อกครบทุกการ์ด")
    void onOrderPlaced_WithMultipleItems_ShouldCheckAllCards() {
        OrderItem item1 = OrderItem.builder().id(1L).inventory(mewtwoInventory).quantity(1).build();
        OrderItem item2 = OrderItem.builder().id(2L).inventory(pikachuInventory).quantity(1).build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(4L)
                .orderCode("ORD-2026-004")
                .items(List.of(item1, item2))
                .build();

        when(cardInventoryRepository.sumQuantityByCardId(100L)).thenReturn(1);
        when(cardInventoryRepository.sumQuantityByCardId(200L)).thenReturn(8);

        lowStockObserver.onOrderPlaced(event);

        verify(cardInventoryRepository).sumQuantityByCardId(100L);
        verify(cardInventoryRepository).sumQuantityByCardId(200L);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อ Event เป็น null ต้องไม่เกิด Exception และไม่เรียก Repository")
    void onOrderPlaced_WhenEventIsNull_ShouldReturnSafely() {
        assertThatCode(() -> lowStockObserver.onOrderPlaced(null))
                .doesNotThrowAnyException();

        verifyNoInteractions(cardInventoryRepository);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อ Items เป็น null หรือ Empty ต้องไม่เรียก Repository")
    void onOrderPlaced_WhenItemsNullOrEmpty_ShouldReturnSafely() {
        OrderPlacedEvent eventWithNullItems = OrderPlacedEvent.builder()
                .orderId(5L)
                .items(null)
                .build();

        OrderPlacedEvent eventWithEmptyItems = OrderPlacedEvent.builder()
                .orderId(6L)
                .items(Collections.emptyList())
                .build();

        lowStockObserver.onOrderPlaced(eventWithNullItems);
        lowStockObserver.onOrderPlaced(eventWithEmptyItems);

        verifyNoInteractions(cardInventoryRepository);
    }

    @Test
    @DisplayName("onOrderPlaced: เมื่อ OrderItem ไม่มี Inventory หรือ Card ต้องข้ามไปอย่างปลอดภัย")
    void onOrderPlaced_WhenItemHasNullInventoryOrCard_ShouldSkipGracefully() {
        OrderItem itemWithNullInventory = OrderItem.builder().id(10L).inventory(null).build();
        OrderItem itemWithNullCard = OrderItem.builder().id(11L).inventory(CardInventory.builder().card(null).build())
                .build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(7L)
                .items(List.of(itemWithNullInventory, itemWithNullCard))
                .build();

        assertThatCode(() -> lowStockObserver.onOrderPlaced(event))
                .doesNotThrowAnyException();

        verify(cardInventoryRepository, never()).sumQuantityByCardId(anyLong());
    }
}
