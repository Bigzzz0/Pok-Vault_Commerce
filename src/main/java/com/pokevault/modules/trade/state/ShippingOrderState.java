package com.pokevault.modules.trade.state;

import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.domain.enums.OrderStatus;

public class ShippingOrderState implements OrderState {

    @Override
    public void complete(OrderContext context) {
        // เมื่อการเทรดในเกมเสร็จสิ้นสมบูรณ์ -> สลับไปสถานะ COMPLETED
        context.setState(new CompletedOrderState());
    }

    @Override
    public void cancel(OrderContext context) {
        // กฎความปลอดภัยสำคัญ: ส่งการ์ดในเกมแล้ว
        // ห้ามยกเลิกเด็ดขาดเพื่อป้องกันการเสียการ์ดฟรี
        throw new InvalidOrderStateException("Cannot cancel order while cards are being shipped in game");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPING;
    }
}
