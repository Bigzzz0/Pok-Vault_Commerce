package com.pokevault.modules.trade.state;

import com.pokevault.common.exception.InvalidOrderStateException;
import com.pokevault.domain.entity.CardInventory;
import com.pokevault.domain.entity.Order;
import com.pokevault.domain.entity.OrderItem;
import com.pokevault.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GoF State Pattern: OrderState Transitions & Business Invariants Test")
class OrderStateTest {

    private Order order;
    private OrderContext context;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .orderCode("ORD-TEST-001")
                .orderStatus(OrderStatus.PENDING)
                .build();

        context = OrderContext.fromOrder(order);
    }

    @Nested
    @DisplayName("1. Happy Path Order Lifecycle Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Order should transition through full happy path: PENDING -> PAID -> SHIPPING -> COMPLETED")
        void testFullOrderLifecycle() {
            // Initial State: PENDING
            assertThat(context.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(context.getCurrentState()).isInstanceOf(PendingOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

            // Action: Pay -> PAID
            context.pay();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(context.getCurrentState()).isInstanceOf(PaidOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

            // Action: Ship -> SHIPPING
            context.ship();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.SHIPPING);
            assertThat(context.getCurrentState()).isInstanceOf(ShippingOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);

            // Action: Complete -> COMPLETED
            context.complete();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(context.getCurrentState()).isInstanceOf(CompletedOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("2. Order Cancellation Flows")
    class CancellationTests {

        @Test
        @DisplayName("Order in PENDING state can be cancelled -> CANCELLED")
        void testCancelFromPending() {
            assertThat(context.getStatus()).isEqualTo(OrderStatus.PENDING);

            context.cancel();

            assertThat(context.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(context.getCurrentState()).isInstanceOf(CancelledOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Cancelling an order returns every item's copies to its inventory")
        void testCancelRestoresStock() {
            CardInventory charizard = CardInventory.builder().id(10L).quantity(0).build();
            CardInventory pikachu = CardInventory.builder().id(11L).quantity(4).build();
            order.getItems().add(OrderItem.builder().order(order).inventory(charizard).quantity(2).build());
            order.getItems().add(OrderItem.builder().order(order).inventory(pikachu).quantity(1).build());

            context.cancel();

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(charizard.getQuantity()).isEqualTo(2);
            assertThat(pikachu.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("Cancelling a PAID order also returns the stock")
        void testCancelFromPaidRestoresStock() {
            CardInventory mewtwo = CardInventory.builder().id(12L).quantity(0).build();
            order.getItems().add(OrderItem.builder().order(order).inventory(mewtwo).quantity(1).build());
            context.pay();

            context.cancel();

            assertThat(mewtwo.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("Order in PAID state can be cancelled before shipping -> CANCELLED")
        void testCancelFromPaid() {
            context.pay();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.PAID);

            context.cancel();

            assertThat(context.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(context.getCurrentState()).isInstanceOf(CancelledOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("3. Critical Business Invariant: Shipping Guard (Anti-Fraud / Free Card Loss Protection)")
    class ShippingGuardTests {

        @Test
        @DisplayName("CRITICAL: Order in SHIPPING state MUST REJECT cancellation to prevent free card loss")
        void testCancelRejectedWhileShipping() {
            context.pay();
            context.ship();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.SHIPPING);

            assertThatThrownBy(() -> context.cancel())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessage("Cannot cancel order while cards are being shipped in game");

            // State and OrderStatus must remain SHIPPING
            assertThat(context.getStatus()).isEqualTo(OrderStatus.SHIPPING);
            assertThat(context.getCurrentState()).isInstanceOf(ShippingOrderState.class);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.SHIPPING);
        }
    }

    @Nested
    @DisplayName("4. Terminal States Protection (COMPLETED & CANCELLED)")
    class TerminalStateTests {

        @Test
        @DisplayName("COMPLETED order must reject all actions (pay, ship, complete, cancel)")
        void testCompletedStateIsTerminal() {
            context.pay();
            context.ship();
            context.complete();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.COMPLETED);

            assertThatThrownBy(() -> context.pay())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot pay order in current state: COMPLETED");

            assertThatThrownBy(() -> context.ship())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot ship order in current state: COMPLETED");

            assertThatThrownBy(() -> context.complete())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot complete order in current state: COMPLETED");

            assertThatThrownBy(() -> context.cancel())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot cancel order in current state: COMPLETED");
        }

        @Test
        @DisplayName("CANCELLED order must reject all actions (pay, ship, complete, cancel)")
        void testCancelledStateIsTerminal() {
            context.cancel();
            assertThat(context.getStatus()).isEqualTo(OrderStatus.CANCELLED);

            assertThatThrownBy(() -> context.pay())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot pay order in current state: CANCELLED");

            assertThatThrownBy(() -> context.ship())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot ship order in current state: CANCELLED");

            assertThatThrownBy(() -> context.complete())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot complete order in current state: CANCELLED");

            assertThatThrownBy(() -> context.cancel())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot cancel order in current state: CANCELLED");
        }
    }

    @Nested
    @DisplayName("5. Illegal State Transitions (Fail-Fast via Default Methods)")
    class IllegalTransitionTests {

        @Test
        @DisplayName("PENDING state must reject invalid actions (ship, complete)")
        void testIllegalActionsInPending() {
            assertThatThrownBy(() -> context.ship())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot ship order in current state: PENDING");

            assertThatThrownBy(() -> context.complete())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot complete order in current state: PENDING");
        }

        @Test
        @DisplayName("PAID state must reject invalid actions (pay, complete)")
        void testIllegalActionsInPaid() {
            context.pay();

            assertThatThrownBy(() -> context.pay())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot pay order in current state: PAID");

            assertThatThrownBy(() -> context.complete())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot complete order in current state: PAID");
        }

        @Test
        @DisplayName("SHIPPING state must reject invalid actions (pay, ship)")
        void testIllegalActionsInShipping() {
            context.pay();
            context.ship();

            assertThatThrownBy(() -> context.pay())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot pay order in current state: SHIPPING");

            assertThatThrownBy(() -> context.ship())
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Cannot ship order in current state: SHIPPING");
        }
    }

    @Nested
    @DisplayName("6. Context Action Execution via String (executeAction)")
    class ExecuteActionStringTests {

        @Test
        @DisplayName("executeAction should execute actions correctly regardless of case or whitespace")
        void testExecuteActionValidStrings() {
            context.executeAction("PAY");
            assertThat(context.getStatus()).isEqualTo(OrderStatus.PAID);

            context.executeAction("  ship  ");
            assertThat(context.getStatus()).isEqualTo(OrderStatus.SHIPPING);

            context.executeAction("CoMpLeTe");
            assertThat(context.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("executeAction should throw InvalidOrderStateException for unsupported or empty action")
        void testExecuteActionInvalidStrings() {
            assertThatThrownBy(() -> context.executeAction("refund"))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Invalid action: 'refund'");

            assertThatThrownBy(() -> context.executeAction(""))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Action parameter is required");

            assertThatThrownBy(() -> context.executeAction(null))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessageContaining("Action parameter is required");
        }
    }

    @Nested
    @DisplayName("7. OrderContext Factory Method (fromOrder)")
    class FactoryMethodTests {

        @Test
        @DisplayName("fromOrder should initialize corresponding State based on order status")
        void testFromOrderStateMapping() {
            Order paidOrder = Order.builder().orderStatus(OrderStatus.PAID).build();
            OrderContext paidCtx = OrderContext.fromOrder(paidOrder);
            assertThat(paidCtx.getCurrentState()).isInstanceOf(PaidOrderState.class);

            Order shippingOrder = Order.builder().orderStatus(OrderStatus.SHIPPING).build();
            OrderContext shippingCtx = OrderContext.fromOrder(shippingOrder);
            assertThat(shippingCtx.getCurrentState()).isInstanceOf(ShippingOrderState.class);

            Order completedOrder = Order.builder().orderStatus(OrderStatus.COMPLETED).build();
            OrderContext completedCtx = OrderContext.fromOrder(completedOrder);
            assertThat(completedCtx.getCurrentState()).isInstanceOf(CompletedOrderState.class);

            Order cancelledOrder = Order.builder().orderStatus(OrderStatus.CANCELLED).build();
            OrderContext cancelledCtx = OrderContext.fromOrder(cancelledOrder);
            assertThat(cancelledCtx.getCurrentState()).isInstanceOf(CancelledOrderState.class);
        }

        @Test
        @DisplayName("fromOrder should throw IllegalArgumentException when order is null")
        void testFromOrderNullOrder() {
            assertThatThrownBy(() -> OrderContext.fromOrder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Order cannot be null");
        }
    }

    @Nested
    @DisplayName("8. OrderContext Constructors & Direct Mutators Tests")
    class OrderContextMutatorTests {

        @Test
        @DisplayName("Default constructor and parameterized constructor should initialize state correctly")
        void testConstructors() {
            OrderContext emptyCtx = new OrderContext();
            assertThat(emptyCtx.getCurrentState()).isNull();
            assertThat(emptyCtx.getOrder()).isNull();
            assertThat(emptyCtx.getStatus()).isNull();

            PendingOrderState pendingState = new PendingOrderState();
            OrderContext stateOnlyCtx = new OrderContext(pendingState);
            assertThat(stateOnlyCtx.getCurrentState()).isSameAs(pendingState);
            assertThat(stateOnlyCtx.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("setState should synchronize status on associated order entity")
        void testSetStateSyncsOrder() {
            Order sampleOrder = Order.builder().orderStatus(OrderStatus.PENDING).build();
            OrderContext ctx = new OrderContext();
            ctx.setOrder(sampleOrder);
            assertThat(ctx.getOrder()).isSameAs(sampleOrder);

            ctx.setState(new PaidOrderState());
            assertThat(sampleOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("Delegated actions on empty context should not throw NullPointerException")
        void testSafeDelegationWhenStateIsNull() {
            OrderContext emptyCtx = new OrderContext();
            emptyCtx.pay();
            emptyCtx.ship();
            emptyCtx.complete();
            emptyCtx.cancel();
            assertThat(emptyCtx.getStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("9. CancelledOrderState Defensive Edge Cases (Null Safety)")
    class CancelledOrderStateEdgeCaseTests {

        @Test
        @DisplayName("CancelledOrderState default constructor logs and does not throw")
        void testDefaultConstructor() {
            CancelledOrderState state = new CancelledOrderState();
            assertThat(state.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("restoreStock safely handles null context and null order")
        void testRestoreStockNullContextAndOrder() {
            CancelledOrderState state = new CancelledOrderState();
            state.restoreStock(null);

            OrderContext ctxWithoutOrder = new OrderContext();
            state.restoreStock(ctxWithoutOrder);
            assertThat(ctxWithoutOrder.getOrder()).isNull();
        }

        @Test
        @DisplayName("restoreStock safely skips items with null inventory or non-positive quantity")
        void testRestoreStockSkipsInvalidItems() {
            CardInventory validInventory = CardInventory.builder().id(50L).quantity(10).build();
            Order testOrder = Order.builder().build();

            // Item 1: null inventory
            testOrder.getItems().add(OrderItem.builder().inventory(null).quantity(2).build());
            // Item 2: null quantity
            testOrder.getItems().add(OrderItem.builder().inventory(validInventory).quantity(null).build());
            // Item 3: zero quantity
            testOrder.getItems().add(OrderItem.builder().inventory(validInventory).quantity(0).build());
            // Item 4: negative quantity
            testOrder.getItems().add(OrderItem.builder().inventory(validInventory).quantity(-1).build());
            // Item 5: valid item
            testOrder.getItems().add(OrderItem.builder().inventory(validInventory).quantity(3).build());

            OrderContext ctx = OrderContext.fromOrder(testOrder);
            CancelledOrderState state = new CancelledOrderState(ctx);

            assertThat(state.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(validInventory.getQuantity()).isEqualTo(13); // only Item 5 was restored (+3)
        }
    }
}
