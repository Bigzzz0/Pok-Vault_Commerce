package com.pokevault.modules.trade.state;

import com.pokevault.domain.entity.Order;
import com.pokevault.domain.enums.OrderStatus;

public class OrderContext {

    private Order order;
    private OrderState currentState;

    public OrderContext() {
    }

    public OrderContext(OrderState initialState) {
        this.currentState = initialState;
    }

    public OrderContext(Order order, OrderState initialState) {
        this.order = order;
        this.currentState = initialState;
    }

    public void setState(OrderState state) {
        this.currentState = state;
    }

    public OrderState getCurrentState() {
        return currentState;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderStatus getStatus() {
        return currentState != null ? currentState.getStatus() : null;
    }

    public void pay() {
        if (currentState != null) {
            currentState.pay(this);
        }
    }

    public void ship() {
        if (currentState != null) {
            currentState.ship(this);
        }
    }

    public void complete() {
        if (currentState != null) {
            currentState.complete(this);
        }
    }

    public void cancel() {
        if (currentState != null) {
            currentState.cancel(this);
        }
    }
}
