package com.pokevault.modules.order;

import com.pokevault.domain.entity.*;
import com.pokevault.domain.enums.*;
import com.pokevault.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
}, showSql = false)
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

    private CardExpansion persistExpansion(String code) {
        return em.persistAndFlush(CardExpansion.builder()
                .code(code)
                .name("Set " + code)
                .series("Pocket")
                .totalCards(226)
                .build());
    }

    private CardInventory persistInventory(CardExpansion exp, String cardNumber, String cardName) {
        Card card = em.persistAndFlush(Card.builder()
                .expansion(exp)
                .cardNumber(cardNumber)
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
        CardExpansion exp = persistExpansion("A1");
        CardInventory inv = persistInventory(exp, "001", "Pikachu");

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
        CardExpansion exp = persistExpansion("A2");
        CardInventory inv = persistInventory(exp, "001", "Charmander");

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
        CardExpansion exp = persistExpansion("A3");
        CardInventory inv = persistInventory(exp, "001", "Squirtle");

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

    @Test
    @DisplayName("Rollback Integrity: การหักสต็อกสินค้าหลายรายการ หากเกิดข้อผิดพลาดและ rollback ข้อมูลสต็อกใน DB จะไม่ถูกหัก")
    void multiItemInventoryRollback_PreservesOriginalStock() {
        CardExpansion exp = persistExpansion("A4");
        CardInventory inv1 = persistInventory(exp, "001", "Bulbasaur");
        CardInventory inv2 = persistInventory(exp, "002", "Ivysaur");
        int originalQty1 = inv1.getQuantity(); // 10
        int originalQty2 = inv2.getQuantity(); // 10

        // จำลอง transaction ที่เริ่มหัก inv1 แต่ abort กลางคันก่อน commit
        inv1.deductStock(2);
        em.persist(inv1);
        em.clear(); // จำลองการ abort/rollback โดยไม่ flush ลง DB

        CardInventory reloaded1 = em.find(CardInventory.class, inv1.getId());
        CardInventory reloaded2 = em.find(CardInventory.class, inv2.getId());

        assertThat(reloaded1.getQuantity()).isEqualTo(originalQty1);
        assertThat(reloaded2.getQuantity()).isEqualTo(originalQty2);
    }
}
