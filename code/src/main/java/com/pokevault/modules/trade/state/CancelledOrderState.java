package com.pokevault.modules.trade.state;

import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * สถานะสิ้นสุด (Terminal State): คำสั่งซื้อถูกยกเลิก (CANCELLED)
 * มีหน้าที่รับผิดชอบกลไกการสั่งคืนสต็อกการ์ดเข้าคลังอัตโนมัติ (Stock
 * Restoration)
 * และป้องกันการเปลี่ยนสถานะใดๆ ซ้ำซ้อน
 */
@Slf4j
public class CancelledOrderState implements OrderState {

    public CancelledOrderState() {
        log.info("Order reached terminal state: CANCELLED without context");
    }

    public CancelledOrderState(OrderContext context) {
        log.info("Order reached terminal state: CANCELLED");
        restoreStock(context);
    }

    /**
     * ดำเนินการคืนสต็อกการ์ดเข้าคลังอัตโนมัติ (Stock Restoration)
     * ป้องกันการสูญเสียสต็อกเมื่อลูกค้าหรือระบบยกเลิกคำสั่งซื้อ
     */
    public void restoreStock(OrderContext context) {
        if (context == null || context.getOrder() == null) {
            log.info("CancelledOrderState: No order attached to context, skipping automatic stock restoration.");
            return;
        }

        Order order = context.getOrder();
        log.info("CancelledOrderState: Triggering automatic stock restoration for Order: {}", order);

        // คืนจำนวนที่ถูกหักตอนจองกลับเข้า CardInventory ของแต่ละรายการ
        // (entity อยู่ใน transaction ของ OrderService จึงถูกบันทึกอัตโนมัติ)
        for (OrderItem item : order.getItems()) {
            CardInventory inventory = item.getInventory();
            if (inventory == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                continue;
            }
            inventory.restoreStock(item.getQuantity());
            log.info("CancelledOrderState: Restored {} copies to inventory id: {} (now {} in vault)",
                    item.getQuantity(), inventory.getId(), inventory.getQuantity());
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
