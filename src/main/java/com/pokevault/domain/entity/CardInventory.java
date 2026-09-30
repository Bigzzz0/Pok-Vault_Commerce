package com.pokevault.domain.entity;

import com.pokevault.common.exception.InsufficientStockException;
import com.pokevault.domain.enums.CardCondition;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "card_inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardInventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_account_id")
    private GameAccount gameAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", nullable = false, length = 30)
    @Builder.Default
    private CardCondition condition = CardCondition.NEAR_MINT;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "buy_in_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal buyInPrice = BigDecimal.ZERO;

    @Column(name = "selling_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "storage_slot", length = 50)
    private String storageSlot;

    public boolean hasSufficientStock(int qty) {
        return this.quantity != null && this.quantity >= qty;
    }

    public void deductStock(int count) {
        if (!hasSufficientStock(count)) {
            throw new InsufficientStockException("Insufficient stock in inventory ID " + this.id + " (Available: " + this.quantity + ", Requested: " + count + ")");
        }
        this.quantity -= count;
    }

    public void restoreStock(int count) {
        if (this.quantity == null) {
            this.quantity = 0;
        }
        this.quantity += count;
    }
}
