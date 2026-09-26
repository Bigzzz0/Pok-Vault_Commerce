package com.pokevault.modules.trade.state;

import com.pokevault.domain.enums.OrderStatus;

public class ShippingOrderState implements OrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPING;
    }
}
