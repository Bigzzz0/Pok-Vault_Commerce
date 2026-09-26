package com.pokevault.domain.entity;

import com.pokevault.domain.enums.AccountTradeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entity representing a store-owned Pokémon TCG Pocket game account (Vault)
 * used for opening booster packs, storing card inventories, and trading with customers.
 */
@Entity
@Table(name = "game_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_code", columnNames = {"account_code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_code", nullable = false, length = 50, unique = true)
    private String accountCode;

    @Column(name = "in_game_name", nullable = false, length = 100)
    private String inGameName;

    @Column(name = "friend_id", nullable = false, length = 50)
    private String friendId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_status", nullable = false, length = 30)
    @Builder.Default
    private AccountTradeStatus tradeStatus = AccountTradeStatus.READY;

    @Column(name = "buy_in_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal buyInCost = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Checks if this game account is currently ready and available for trade matching.
     *
     * @return true if status is READY
     */
    public boolean isAvailableForTrade() {
        return this.tradeStatus == AccountTradeStatus.READY;
    }

    /**
     * Updates account status to BUSY_TRADING when assigned to an active trade order.
     */
    public void markBusyTrading() {
        this.tradeStatus = AccountTradeStatus.BUSY_TRADING;
    }

    /**
     * Sets account status to COOLDOWN after completing a trade in-game.
     */
    public void markCooldown() {
        this.tradeStatus = AccountTradeStatus.COOLDOWN;
    }

    /**
     * Restores account status to READY when available for new trades.
     */
    public void markReady() {
        this.tradeStatus = AccountTradeStatus.READY;
    }

    /**
     * Suspends account usage with an optional reason recorded in notes.
     *
     * @param reason the suspension reason
     */
    public void suspend(String reason) {
        this.tradeStatus = AccountTradeStatus.SUSPENDED;
        if (reason != null && !reason.isBlank()) {
            this.notes = reason;
        }
    }
}
