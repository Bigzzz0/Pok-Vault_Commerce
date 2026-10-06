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
 * พร้อมระบบ Threshold Guard
 * ตรวจจับและแจ้งเตือนเมื่อสต็อกเหลือน้อยกว่าหรือเท่ากับ 2 ใบ
 * 
 * ผู้รับผิดชอบ: สมาชิกคนที่ 2 (Game Account Vault & Inventory Manager)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockObserver {

    /**
     * เกณฑ์สต็อกต่ำ (Low Stock Threshold): หากการ์ดคงเหลือ <= 2 ใบ
     * จะส่งสัญญาณแจ้งเตือน
     */
    public static final int LOW_STOCK_THRESHOLD = 2;

    private final CardInventoryRepository cardInventoryRepository;

    /**
     * ดักฟังสัญญาณ Event เมื่อมีคำสั่งซื้อเสร็จสมบูรณ์
     * ทำการคำนวณยอดรวมสต็อกคงเหลือ และแจ้งเตือน Warning หากต่ำกว่าเกณฑ์
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

                if (remainingStock <= LOW_STOCK_THRESHOLD) {
                    log.warn("⚠️ LOW STOCK ALERT: Card '{}' ({}) has only {} copies left in vault (threshold: {})!",
                            card.getName(), card.getCardNumber(), remainingStock, LOW_STOCK_THRESHOLD);
                } else {
                    log.info("Card '{}' ({}) stock is sufficient: {} copies remaining in vault.",
                            card.getName(), card.getCardNumber(), remainingStock);
                }
            }
        }
    }
}
