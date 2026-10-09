package com.pokevault.modules.order;

import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
}, showSql = false)
@ActiveProfiles("test")
@DisplayName("OrderPersistenceTest: Member 3 Order & OrderItem JPA Mapping and Cascades")
class OrderPersistenceTest {

    @Autowired private TestEntityManager em;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    private User persistUser(String username) {
        return em.persistAndFlush(User.builder()
                .username(username)
                .email(username + "@test.com")
                .password("hash-123")
                .build());
    }

    private CardInventory persistInventory(String cardName) {
        CardExpansion exp = em.persistAndFlush(CardExpansion.builder()
                .code("A1")
                .name("Genetic Apex")
                .series("Pocket")
                .totalCards(226)
                .build());

        Card card = em.persistAndFlush(Card.builder()
                .expansion(exp)
                .cardNumber("001")
                .name(cardName)
                .cardType(CardType.POKEMON)
                .rarity(Rarity.COMMON)
                .elementType(ElementType.LIGHTNING)
                .hp(60)
                .build());

        return em.persistAndFlush(CardInventory.builder()
                .card(card)
                .condition(CardCondition.MINT)
                .quantity(10)
                .sellingPrice(new BigDecimal("250.00"))
                .build());
    }

    @Test
    @DisplayName("JPA CascadeType.ALL: บันทึก Order จะทำการ Cascade บันทึก OrderItem ลงฐานข้อมูลอัตโนมัติ")
    void orderAndItems_CascadePersistSuccessfully() {
        User user = persistUser("customer1");
        CardInventory inv = persistInventory("Pikachu");

        Order order = Order.builder()
                .orderCode("ORD-2026-001")
                .user(user)
                .customerFriendId("1234-5678-9012-3456")
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .discountAmount(new BigDecimal("50.00"))
                .finalAmount(new BigDecimal("450.00"))
                .notes("Express delivery")
                .build();

        OrderItem item = OrderItem.builder()
                .inventory(inv)
                .quantity(2)
                .unitPrice(new BigDecimal("250.00"))
                .subtotal(new BigDecimal("500.00"))
                .tradeStatus(TradeFulfillmentStatus.UNASSIGNED)
                .build();

        order.addItem(item);
        Order saved = orderRepository.saveAndFlush(order);

        Long orderId = saved.getId();
        em.clear();

        Order found = orderRepository.findById(orderId).orElseThrow();
        assertThat(found.getOrderCode()).isEqualTo("ORD-2026-001");
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(found.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(found.getItems().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(found.getItems().get(0).getOrder().getId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("JPA orphanRemoval: ลบ OrderItem ออกจาก Order.items จะลบ record ในฐานข้อมูลจริง")
    void orphanRemoval_DeletesItemWhenRemovedFromOrder() {
        User user = persistUser("customer2");
        CardInventory inv = persistInventory("Charmander");

        Order order = Order.builder()
                .orderCode("ORD-2026-002")
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("500.00"))
                .finalAmount(new BigDecimal("500.00"))
                .build();

        OrderItem item1 = OrderItem.builder().inventory(inv).quantity(1).unitPrice(new BigDecimal("250.00")).subtotal(new BigDecimal("250.00")).build();
        OrderItem item2 = OrderItem.builder().inventory(inv).quantity(1).unitPrice(new BigDecimal("250.00")).subtotal(new BigDecimal("250.00")).build();
        order.addItem(item1);
        order.addItem(item2);

        order = orderRepository.saveAndFlush(order);
        Long orderId = order.getId();
        em.clear();

        Order loaded = orderRepository.findById(orderId).orElseThrow();
        assertThat(loaded.getItems()).hasSize(2);

        // ลบรายการแรกออก
        loaded.getItems().remove(0);
        orderRepository.saveAndFlush(loaded);
        em.clear();

        Order reloaded = orderRepository.findById(orderId).orElseThrow();
        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(orderItemRepository.findByOrderId(orderId)).hasSize(1);
    }

    @Test
    @DisplayName("OrderRepository: findByOrderCode ค้นหาออเดอร์ด้วยรหัสโค้ดเฉพาะได้ถูกต้อง")
    void findByOrderCode_ReturnsMatchingOrder() {
        User user = persistUser("customer3");
        Order order = Order.builder().orderCode("ORD-2026-777").user(user).orderStatus(OrderStatus.PAID).build();
        orderRepository.saveAndFlush(order);
        em.clear();

        assertThat(orderRepository.findByOrderCode("ORD-2026-777")).isPresent();
        assertThat(orderRepository.findByOrderCode("NON_EXISTING")).isEmpty();
    }

    @Test
    @DisplayName("OrderRepository: findByUserId ค้นหาคำสั่งซื้อทั้งหมดของลูกค้ารายนั้น")
    void findByUserId_ReturnsUserOrders() {
        User userA = persistUser("customerA");
        User userB = persistUser("customerB");

        orderRepository.saveAndFlush(Order.builder().orderCode("ORD-2026-101").user(userA).build());
        orderRepository.saveAndFlush(Order.builder().orderCode("ORD-2026-102").user(userA).build());
        orderRepository.saveAndFlush(Order.builder().orderCode("ORD-2026-103").user(userB).build());
        em.clear();

        List<Order> ordersA = orderRepository.findByUserId(userA.getId());
        List<Order> ordersB = orderRepository.findByUserId(userB.getId());

        assertThat(ordersA).hasSize(2);
        assertThat(ordersB).hasSize(1);
    }

    @Test
    @DisplayName("OrderItemRepository: findByOrderId ค้นหารายการสินค้าตามไอดีออเดอร์")
    void findByOrderId_ReturnsAllItems() {
        User user = persistUser("customer4");
        CardInventory inv = persistInventory("Squirtle");

        Order order = Order.builder().orderCode("ORD-2026-200").user(user).build();
        order.addItem(OrderItem.builder().inventory(inv).quantity(3).unitPrice(new BigDecimal("100.00")).subtotal(new BigDecimal("300.00")).build());
        Order saved = orderRepository.saveAndFlush(order);
        em.clear();

        List<OrderItem> items = orderItemRepository.findByOrderId(saved.getId());
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Audit Timestamps: createdAt และ updatedAt ถูกสร้างอัตโนมัติบน Order Entity")
    void auditTimestamps_AutomaticallyCreated() {
        User user = persistUser("customer5");
        Order order = Order.builder().orderCode("ORD-2026-300").user(user).build();
        Order saved = orderRepository.saveAndFlush(order);
        em.clear();

        Order found = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
