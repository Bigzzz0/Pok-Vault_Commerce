package com.pokevault.domain.entity;

import com.pokevault.domain.enums.AccountTradeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "game_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_code", nullable = false, unique = true, length = 50)
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
}
