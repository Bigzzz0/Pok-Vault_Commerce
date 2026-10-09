package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;

public class PaidOrderState implements OrderState {

    @Override
    public void ship(OrderContext context) {
        // เมื่อเริ่มกระบวนการส่งมอบการ์ดในเกม (Trade Sent) -> สลับเป็น SHIPPING
        context.setState(new ShippingOrderState());
    }

    @Override
    public void cancel(OrderContext context) {
        // ลูกค้ายกเลิกก่อนเริ่มส่งการ์ดในเกม (คืนเงิน) -> สลับเป็น CANCELLED
        // พร้อมคืนสต็อก
        context.setState(new CancelledOrderState(context));
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PAID;
    }
}
