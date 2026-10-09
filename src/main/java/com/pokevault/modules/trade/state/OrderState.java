package com.pokevault.modules.trade.state;

import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.domain.enums.OrderStatus;

public interface OrderState {

    /**
     * ดำเนินการชำระเงินสำหรับคำสั่งซื้อ
     */
    default void pay(OrderContext context) {
        throw new InvalidOrderStateException("Cannot pay order in current state: " + getStatus());
    }

    /**
     * ดำเนินการส่งมอบการ์ดในเกม (Trade Sent)
     */
    default void ship(OrderContext context) {
        throw new InvalidOrderStateException("Cannot ship order in current state: " + getStatus());
    }

    /**
     * ดำเนินการปิดออเดอร์เมื่อลูกค้ากดยืนยันรับการ์ดครบถ้วน
     */
    default void complete(OrderContext context) {
        throw new InvalidOrderStateException("Cannot complete order in current state: " + getStatus());
    }

    /**
     * ดำเนินการขอยกเลิกคำสั่งซื้อ
     */
    default void cancel(OrderContext context) {
        throw new InvalidOrderStateException("Cannot cancel order in current state: " + getStatus());
    }

    /**
     * ดึงค่า Enum OrderStatus ที่ตรงกับ State ปัจจุบัน
     */
    OrderStatus getStatus();
}
