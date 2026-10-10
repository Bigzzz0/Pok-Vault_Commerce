package com.pokevault.modules.trade.state;

import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.common.exception.TradeStateConflictException;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.enums.OrderStatus;
import com.pokevault.domain.enums.TradeFulfillmentStatus;

public class ShippingOrderState implements OrderState {

    @Override
    public void complete(OrderContext context) {
        if (context != null && context.getOrder() != null) {
            Order order = context.getOrder();
            if (order.getItems() == null || order.getItems().isEmpty()) {
                throw new TradeStateConflictException("Cannot complete order: order has no items");
            }
            boolean allItemsCompleted = order.getItems().stream()
                    .allMatch(item -> item.getTradeStatus() == TradeFulfillmentStatus.COMPLETED);
            if (!allItemsCompleted) {
                throw new TradeStateConflictException(
                        "Cannot complete order: not all items have reached COMPLETED trade status");
            }
        }
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
