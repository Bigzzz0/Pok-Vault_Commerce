package com.pokevault.domain.entity;

import com.pokevault.domain.enums.TradeFulfillmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private CardInventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_account_id")
    private GameAccount assignedAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", length = 30)
    @Builder.Default
    private TradeFulfillmentStatus tradeStatus = TradeFulfillmentStatus.UNASSIGNED;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;
}
