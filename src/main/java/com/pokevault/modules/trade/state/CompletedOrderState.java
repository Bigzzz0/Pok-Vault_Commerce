package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;

public class CompletedOrderState implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.COMPLETED;
    }
}
