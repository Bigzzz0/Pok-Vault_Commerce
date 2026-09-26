package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;

public class PendingOrderState implements OrderState {

    @Override
    public void pay(OrderContext context) {
        // เมื่อลูกค้าชำระเงิน หรือแอดมินยืนยันสลิปโอนเงิน -> สลับเป็น PAID
        context.setState(new PaidOrderState());
    }

    @Override
    public void cancel(OrderContext context) {
        // เมื่อลูกค้ายกเลิกคำสั่งซื้อ หรือไม่โอนในเวลาที่กำหนด -> สลับเป็น CANCELLED
        context.setState(new CancelledOrderState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}
