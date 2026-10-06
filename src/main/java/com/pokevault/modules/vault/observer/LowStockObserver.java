package com.pokevault.modules.vault.observer;

import com.pokevault.domain.entity.Card;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.modules.order.event.OrderPlacedEvent;
import com.pokevault.repository.CardInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * GoF Observer Pattern: Concrete Observer / Event Listener
 * ดักฟังสัญญาณ OrderPlacedEvent เพื่อมอนิเตอร์ระดับสต็อกการ์ดในคลัง
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockObserver {

    private final CardInventoryRepository cardInventoryRepository;

    /**
     * ดักฟังสัญญาณ Event เมื่อมีคำสั่งซื้อเกิดขึ้นในระบบ (Observer Handler)
     *
     * @param event ข้อมูลคำสั่งซื้อและรายการการ์ดที่ถูกสั่งซื้อจาก Order Engine
     */
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        if (event == null || event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }

        log.info("Received OrderPlacedEvent for order: code={}, checking inventory levels...",
                event.getOrderCode());

        for (OrderItem item : event.getItems()) {
            CardInventory inventory = item.getInventory();
            if (inventory != null && inventory.getCard() != null) {
                Card card = inventory.getCard();
                int remainingStock = cardInventoryRepository.sumQuantityByCardId(card.getId());

                log.info("Inspecting card '{}' ({}) total remaining stock in vault: {}",
                        card.getName(), card.getCardNumber(), remainingStock);
            }
        }
    }
}
