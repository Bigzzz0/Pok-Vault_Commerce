package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;

public class CancelledOrderState implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
