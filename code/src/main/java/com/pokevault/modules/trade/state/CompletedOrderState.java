package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * สถานะสิ้นสุด (Terminal State): คำสั่งซื้อเสร็จสมบูรณ์เรียบร้อยแล้ว
 * (COMPLETED)
 * ลูกค้ายืนยันการรับการ์ดในเกมครบถ้วน ไม่อนุญาตให้เปลี่ยนสถานะหรือดำเนินการใดๆ
 * ต่อไป
 */
@Slf4j
public class CompletedOrderState implements OrderState {

    public CompletedOrderState() {
        log.info("Order reached terminal state: COMPLETED");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.COMPLETED;
    }
}
