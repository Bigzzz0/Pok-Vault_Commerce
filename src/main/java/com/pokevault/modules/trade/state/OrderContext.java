package com.pokevault.modules.trade.state;

import com.pokevault.common.exception.InvalidOrderStateException;
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

    /**
     * Factory Method: สร้าง OrderContext จาก Entity Order โดยระบุ State
     * เริ่มต้นตามสถานะปัจจุบันของออเดอร์
     */
    public static OrderContext fromOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        OrderStatus status = order.getOrderStatus() != null ? order.getOrderStatus() : OrderStatus.PENDING;
        OrderState initialState = switch (status) {
            case PENDING -> new PendingOrderState();
            case PAID -> new PaidOrderState();
            case SHIPPING -> new ShippingOrderState();
            case COMPLETED -> new CompletedOrderState();
            case CANCELLED -> new CancelledOrderState();
        };

        return new OrderContext(order, initialState);
    }

    /**
     * ดำเนินการ Action ตามพารามิเตอร์สตริง (pay, ship, complete, cancel)
     */
    public void executeAction(String action) {
        if (action == null || action.isBlank()) {
            throw new InvalidOrderStateException("Action parameter is required and cannot be empty");
        }

        switch (action.trim().toLowerCase()) {
            case "pay" -> pay();
            case "ship" -> ship();
            case "complete" -> complete();
            case "cancel" -> cancel();
            default -> throw new InvalidOrderStateException(
                    "Invalid action: '" + action + "'. Valid actions are: pay, ship, complete, cancel");
        }
    }

    public void setState(OrderState state) {
        this.currentState = state;
        if (this.order != null && state != null && state.getStatus() != null) {
            this.order.setOrderStatus(state.getStatus());
        }
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
